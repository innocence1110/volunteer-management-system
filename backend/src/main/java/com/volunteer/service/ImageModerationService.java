package com.volunteer.service;

import org.springframework.stereotype.Service;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片审核服务 —— 多层图像识别算法
 */
@Service
public class ImageModerationService {

    // =============== 可调阈值 ===============
    private static final int    MIN_DIMENSION        = 64;
    private static final double MIN_ASPECT_RATIO     = 0.1;
    private static final double MAX_ASPECT_RATIO     = 10.0;
    private static final double SKIN_RATIO_THRESHOLD = 0.22;     // 全局肤色比例上限
    private static final double SKIN_SPREAD_THRESHOLD = 0.10;    // 皮肤扩散拦截阈值
    private static final double SKIN_CELL_THRESHOLD  = 0.32;     // 单格肤色密度阈值
    private static final double BLUR_THRESHOLD       = 15.0;     // 拉普拉斯方差下限（低于此值视为模糊）
    private static final double BRIGHTNESS_LOW       = 0.08;     // 平均亮度下限
    private static final double BRIGHTNESS_HIGH      = 0.95;     // 平均亮度上限
    private static final double EDGE_DENSITY_HIGH    = 0.18;     // Sobel 边缘密度上限
    private static final double UNIFORM_THRESHOLD    = 0.92;     // 纯色区域占比阈值

    /** 审核结果 */
    public static class ModerationResult {
        private final boolean  passed;
        private final String   reason;
        private final double   score;
        private final List<String> details;

        public ModerationResult(boolean passed, String reason, double score, List<String> details) {
            this.passed  = passed;
            this.reason  = reason;
            this.score   = score;
            this.details = details;
        }

        public boolean  isPassed()  { return passed; }
        public String   getReason() { return reason; }
        public double   getScore()  { return score;  }
        public List<String> getDetails() { return details; }
    }

    /**
     * 全链路图片审核
     */
    public ModerationResult moderate(BufferedImage image) {
        List<String> details = new ArrayList<>();
        double totalScore = 0.0;
        int checks = 0;

        int w = image.getWidth();
        int h = image.getHeight();

        // Layer 1: 基础校验
        if (w < MIN_DIMENSION || h < MIN_DIMENSION) {
            details.add(String.format("图片尺寸过小 (%dx%d), 需至少 %dx%d", w, h, MIN_DIMENSION, MIN_DIMENSION));
            return reject("图片尺寸过小，请上传清晰照片", 1.0, details);
        }
        double aspect = (double) Math.max(w, h) / Math.min(w, h);
        if (aspect > MAX_ASPECT_RATIO || aspect < MIN_ASPECT_RATIO) {
            details.add(String.format("宽高比异常 (%.2f)", aspect));
            return reject("图片宽高比异常，请重新拍摄", 0.8, details);
        }

        // Layer 2: 图像质量检测
        double brightness = computeBrightness(image);
        if (brightness < BRIGHTNESS_LOW) return reject("图片过暗，请重新拍摄", 0.7, details);
        if (brightness > BRIGHTNESS_HIGH) return reject("图片过亮或全白，请重新拍摄", 0.7, details);

        double blurScore = detectBlur(image);
        if (blurScore < BLUR_THRESHOLD) return reject("图片模糊不清，请重新拍摄", 0.6, details);
        totalScore += Math.max(0, 1.0 - blurScore / 100.0);
        checks++;

        // Layer 3: 肤色检测
        SkinAnalysis skinAnalysis = analyzeSkin(image);
        double skinRatio = skinAnalysis.globalRatio;
        int skinSpreadRows = skinAnalysis.spreadRows;
        details.add(String.format("肤色占比: %.2f%%, 扩散行数: %d", skinRatio * 100, skinSpreadRows));

        if (skinRatio > SKIN_RATIO_THRESHOLD || (skinRatio > SKIN_SPREAD_THRESHOLD && skinSpreadRows >= 3)) {
            return reject("图片包含违规内容（暴露/不雅），请上传合规照片", 0.7, details);
        }
        totalScore += skinRatio * 2.0;
        checks++;

        // Layer 4: 纹理密度检测（Sobel 边缘密度）
        double edgeDensity = computeEdgeDensity(image);
        if (edgeDensity > EDGE_DENSITY_HIGH) {
            return reject("图片包含过多文字或二维码，请上传活动现场照片", 0.7, details);
        }
        totalScore += edgeDensity;
        checks++;

        // Layer 5: 高频翻转检测（二维码检测）
        double highFreq = detectHighFrequency(image);
        if (highFreq > 0.38) {
            return reject("检测到二维码或密集文字，请上传活动现场照片", 0.75, details);
        }
        totalScore += highFreq;
        checks++;

        // Layer 6: 纯色区域检测
        double uniformRatio = detectUniformRegion(image);
        if (uniformRatio > UNIFORM_THRESHOLD) {
            return reject("图片为纯色/重复图案，请上传真实的现场照片", 0.8, details);
        }
        totalScore += uniformRatio;
        checks++;

        // 综合评分
        double finalScore = checks > 0 ? totalScore / checks : 0.0;
        return new ModerationResult(true, "审核通过", finalScore, details);
    }

    // ====================== 私有检测方法 ======================

    /**
     * 计算平均亮度 (0~1)
     */
    private double computeBrightness(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        long sum = 0;
        int pixels = w * h;

        int step = Math.max(1, (int) Math.sqrt(pixels / 10000.0));
        int count = 0;

        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                Color c = new Color(image.getRGB(x, y));
                int lum = (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
                sum += lum;
                count++;
            }
        }
        return count > 0 ? (sum / (double) count) / 255.0 : 0.5;
    }

    /**
     * 拉普拉斯方差模糊检测
     * 清晰图片方差大，模糊图片方差小
     */
    private double detectBlur(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        int[][] kernel = {{0, -1, 0}, {-1, 4, -1}, {0, -1, 0}};
        double sum = 0, sumSq = 0;
        int count = 0;

        int step = Math.max(1, (int) Math.sqrt(w * h / 5000.0));

        for (int y = 1; y < h - 1; y += step) {
            for (int x = 1; x < w - 1; x += step) {
                int laplacian = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color c = new Color(image.getRGB(x + kx, y + ky));
                        int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                        laplacian += gray * kernel[ky + 1][kx + 1];
                    }
                }
                sum += laplacian;
                sumSq += (double) laplacian * laplacian;
                count++;
            }
        }

        if (count == 0) return 100;
        double mean = sum / count;
        return (sumSq / count) - (mean * mean);
    }

    /** 肤色分析结果 */
    private static class SkinAnalysis {
        final double globalRatio;       // 全图肤色像素占比
        final int    spreadRows;        // 8行网格中肤色密度超过阈值的行数

        SkinAnalysis(double globalRatio, int spreadRows) {
            this.globalRatio = globalRatio;
            this.spreadRows  = spreadRows;
        }
    }

    /**
     * HSV 肤色检测 + 8×4 网格扩散分析
     * 自拍皮肤集中在顶部1-2行（脸），泳装照皮肤扩散到3-6行（脸+肩+臂+胸）
     */
    private SkinAnalysis analyzeSkin(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        int rows = 8, cols = 4;
        int cellH = h / rows, cellW = w / cols;
        int step = Math.max(1, (int) Math.sqrt(w * h / 20000.0));

        double[] rowSkinRatio = new double[rows];
        long globalSkinPixels = 0, globalTotalPixels = 0;

        for (int gy = 0; gy < rows; gy++) {
            int startY = gy * cellH;
            int endY = (gy == rows - 1) ? h : (gy + 1) * cellH;
            long rowSkin = 0, rowTotal = 0;

            for (int y = startY; y < endY; y += step) {
                for (int x = 0; x < w; x += step) {
                    Color c = new Color(image.getRGB(x, y));
                    float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);

                    float hue = hsv[0] * 360;
                    float sat = hsv[1] * 255;
                    float val = hsv[2] * 255;

                    // HSV 肤色判定（支持亚洲/浅色/深色/偏红皮肤）
                    boolean isSkin = (hue >= 0 && hue <= 50 && sat >= 20 && sat <= 150 && val >= 50)
                                  || (hue >= 0 && hue <= 50 && sat >= 20 && val >= 20 && val <= 200)
                                  || (hue >= 330 && hue <= 360 && sat >= 15 && sat <= 120 && val >= 50);
                    // 排除纯黑白/灰阶像素
                    if (sat < 10 || val < 10 || val > 245) isSkin = false;

                    if (isSkin) { rowSkin++; globalSkinPixels++; }
                    rowTotal++; globalTotalPixels++;
                }
            }
            rowSkinRatio[gy] = rowTotal > 0 ? (double) rowSkin / rowTotal : 0;
        }

        int spreadCount = 0;
        for (int gy = 0; gy < rows; gy++) {
            if (rowSkinRatio[gy] > SKIN_CELL_THRESHOLD) spreadCount++;
        }

        double globalRatio = globalTotalPixels > 0 ? (double) globalSkinPixels / globalTotalPixels : 0;
        return new SkinAnalysis(globalRatio, spreadCount);
    }

    /**
     * Sobel 边缘密度检测 —— 识别二维码/文字图
     */
    private double computeEdgeDensity(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        int step = Math.max(1, (int) Math.sqrt(w * h / 50000.0));
        int edgePixels = 0, totalPixels = 0;

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
        int threshold = 50;

        for (int y = 1; y < h - 1; y += step) {
            for (int x = 1; x < w - 1; x += step) {
                int gx = 0, gy = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color c = new Color(image.getRGB(x + kx, y + ky));
                        int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                        gx += gray * sobelX[ky + 1][kx + 1];
                        gy += gray * sobelY[ky + 1][kx + 1];
                    }
                }
                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                if (magnitude > threshold) edgePixels++;
                totalPixels++;
            }
        }

        return totalPixels > 0 ? (double) edgePixels / totalPixels : 0.0;
    }

    /**
     * 高频翻转检测 —— 滑动窗口法识别二维码
     * 二维码模块交替排列，相邻像素频繁翻转（>35%），自然照片过渡平缓（<10%）
     */
    private double detectHighFrequency(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        int scanRows = Math.min(30, h);
        int rowStep = Math.max(1, h / scanRows);
        int colStep = 3;
        int windowSize = 30;
        int flipThreshold = 80;

        double maxRate = 0;

        for (int y = 0; y < h; y += rowStep) {
            int sampleCount = (w + colStep - 1) / colStep;
            int[] grays = new int[sampleCount];
            int idx = 0;

            for (int x = 0; x < w; x += colStep) {
                Color c = new Color(image.getRGB(x, y));
                grays[idx++] = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
            }

            int maxPairs = sampleCount - 1;
            for (int start = 0; start + windowSize <= maxPairs; start++) {
                int flips = 0;
                for (int k = 0; k < windowSize; k++) {
                    if (Math.abs(grays[start + k] - grays[start + k + 1]) > flipThreshold) flips++;
                }
                double rate = (double) flips / windowSize;
                if (rate > maxRate) {
                    maxRate = rate;
                    if (maxRate > 0.30) return maxRate;
                }
            }
        }

        return maxRate;
    }

    /**
     * 4×4 网格颜色方差检测 —— 识别纯色/重复图案作弊图
     */
    private double detectUniformRegion(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int step = Math.max(1, (int) Math.sqrt(w * h / 4000.0));

        int gridCols = 4, gridRows = 4;
        int cellW = Math.max(1, w / gridCols), cellH = Math.max(1, h / gridRows);
        int uniformCells = 0, totalCells = 0;

        for (int gy = 0; gy < gridRows; gy++) {
            for (int gx = 0; gx < gridCols; gx++) {
                int startX = gx * cellW, startY = gy * cellH;
                int endX = Math.min((gx + 1) * cellW, w);
                int endY = Math.min((gy + 1) * cellH, h);

                long sumR = 0, sumG = 0, sumB = 0;
                int count = 0;

                for (int y = startY; y < endY; y += step) {
                    for (int x = startX; x < endX; x += step) {
                        Color c = new Color(image.getRGB(x, y));
                        sumR += c.getRed(); sumG += c.getGreen(); sumB += c.getBlue();
                        count++;
                    }
                }

                if (count == 0) continue;

                double avgR = (double) sumR / count, avgG = (double) sumG / count, avgB = (double) sumB / count;
                double varSum = 0;
                int varCount = 0;

                for (int y = startY; y < endY; y += step) {
                    for (int x = startX; x < endX; x += step) {
                        Color c = new Color(image.getRGB(x, y));
                        varSum += Math.pow(c.getRed() - avgR, 2) + Math.pow(c.getGreen() - avgG, 2) + Math.pow(c.getBlue() - avgB, 2);
                        varCount++;
                    }
                }

                double variance = varCount > 0 ? varSum / varCount / (255.0 * 255.0 * 3) : 0;
                if (variance < 0.02) uniformCells++;
                totalCells++;
            }
        }

        return totalCells > 0 ? (double) uniformCells / totalCells : 0.0;
    }

    private ModerationResult reject(String reason, double score, List<String> details) {
        return new ModerationResult(false, reason, score, details);
    }
}

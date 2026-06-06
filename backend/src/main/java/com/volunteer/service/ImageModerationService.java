package com.volunteer.service;

import org.springframework.stereotype.Service;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片审核服务 —— 多层图像识别算法
 *
 * 检测流程：
 *   1. 基础校验（尺寸、宽高比）
 *   2. 图像质量检测（模糊度、亮度异常）
 *   3. 肤色区域检测（HSV 颜色空间，识别涉黄/暴露内容）
 *   4. 纹理密度检测（识别二维码/纯色图等作弊行为）
 */
@Service
public class ImageModerationService {

    // =============== 可调阈值 ===============
    private static final int    MIN_DIMENSION        = 64;       // 最小宽或高
    private static final double MIN_ASPECT_RATIO     = 0.1;      // 最扁比例
    private static final double MAX_ASPECT_RATIO     = 10.0;     // 最窄比例
    private static final double SKIN_RATIO_THRESHOLD = 0.35;     // 肤色比例上限
    private static final double BLUR_THRESHOLD       = 15.0;     // 拉普拉斯方差下限（低于此值视为模糊）
    private static final double BRIGHTNESS_LOW       = 0.08;     // 平均亮度下限（太暗）
    private static final double BRIGHTNESS_HIGH      = 0.95;     // 平均亮度上限（太亮）
    private static final double EDGE_DENSITY_HIGH    = 0.40;     // 边缘密度过高 -> 可能是二维码/文字图
    private static final double UNIFORM_THRESHOLD    = 0.92;     // 单色区域占比 -> 纯色作弊图

    /**
     * 审核结果
     */
    public static class ModerationResult {
        private final boolean  passed;
        private final String   reason;
        private final double   score;        // 0.0 ~ 1.0, 越高越可能违规
        private final List<String> details;  // 各层检测详情

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
     * 入口：对图片进行全链路审核
     * @param image 待审核图片
     * @return 审核结果
     */
    public ModerationResult moderate(BufferedImage image) {
        List<String> details = new ArrayList<>();
        double totalScore = 0.0;
        int checks = 0;

        int w = image.getWidth();
        int h = image.getHeight();

        // ── Layer 1: 基础校验 ──
        if (w < MIN_DIMENSION || h < MIN_DIMENSION) {
            details.add(String.format("图片尺寸过小 (%dx%d), 需至少 %dx%d", w, h, MIN_DIMENSION, MIN_DIMENSION));
            return reject("图片尺寸过小，请上传清晰照片", 1.0, details);
        }
        double aspect = (double) Math.max(w, h) / Math.min(w, h);
        if (aspect > MAX_ASPECT_RATIO || aspect < MIN_ASPECT_RATIO) {
            details.add(String.format("宽高比异常 (%.2f)", aspect));
            return reject("图片宽高比异常，请重新拍摄", 0.8, details);
        }
        details.add(String.format("基础校验通过 (%dx%d, 比例 %.2f)", w, h, aspect));

        // ── Layer 2: 图像质量检测 ──
        double brightness = computeBrightness(image);
        details.add(String.format("平均亮度: %.3f", brightness));

        if (brightness < BRIGHTNESS_LOW) {
            return reject("图片过暗，请重新拍摄", 0.7, details);
        }
        if (brightness > BRIGHTNESS_HIGH) {
            return reject("图片过亮或全白，请重新拍摄", 0.7, details);
        }

        double blurScore = detectBlur(image);
        details.add(String.format("模糊度评分: %.2f", blurScore));
        if (blurScore < BLUR_THRESHOLD) {
            return reject("图片模糊不清，请重新拍摄", 0.6, details);
        }
        totalScore += Math.max(0, 1.0 - blurScore / 100.0);
        checks++;

        // ── Layer 3: 肤色检测（识别涉黄/暴露内容） ──
        double skinRatio = detectSkinColor(image);
        details.add(String.format("肤色区域占比: %.2f%%", skinRatio * 100));

        if (skinRatio > SKIN_RATIO_THRESHOLD) {
            double severity = Math.min(1.0, (skinRatio - SKIN_RATIO_THRESHOLD) / 0.3);
            return reject("图片包含违规内容（暴露/不雅），请上传合规照片", severity, details);
        }
        totalScore += skinRatio * 2.0;
        checks++;

        // ── Layer 4: 纹理密度检测 ──
        double edgeDensity = computeEdgeDensity(image);
        details.add(String.format("边缘密度: %.3f", edgeDensity));

        if (edgeDensity > EDGE_DENSITY_HIGH) {
            return reject("图片包含过多文字或二维码，请上传活动现场照片", 0.7, details);
        }
        totalScore += edgeDensity;
        checks++;

        // ── Layer 5: 单色/纯色检测 ──
        double uniformRatio = detectUniformRegion(image);
        details.add(String.format("单色区域占比: %.2f%%", uniformRatio * 100));
        if (uniformRatio > UNIFORM_THRESHOLD) {
            return reject("图片为纯色/重复图案，请上传真实的现场照片", 0.8, details);
        }
        totalScore += uniformRatio;
        checks++;

        // ── 综合判定 ──
        double finalScore = checks > 0 ? totalScore / checks : 0.0;
        details.add(String.format("综合评分: %.3f", finalScore));

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

        // 采样，避免全量遍历大图
        int step = Math.max(1, (int) Math.sqrt(pixels / 10000.0));

        int count = 0;
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                Color c = new Color(image.getRGB(x, y));
                // 相对亮度公式: 0.299R + 0.587G + 0.114B
                int lum = (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
                sum += lum;
                count++;
            }
        }
        return count > 0 ? (sum / (double) count) / 255.0 : 0.5;
    }

    /**
     * 模糊检测 —— 简化版的拉普拉斯方差
     *
     * 原理：清晰图片相邻像素差异大，拉普拉斯响应方差大；
     *       模糊图片像素变化平缓，方差小。
     * 阈值参考：< 15 严重模糊，15~30 轻微模糊，> 30 清晰
     */
    private double detectBlur(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        // 3x3 拉普拉斯核近似
        int[][] kernel = {{0, -1, 0}, {-1, 4, -1}, {0, -1, 0}};

        double sum = 0;
        double sumSq = 0;
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
        return (sumSq / count) - (mean * mean);  // 方差
    }

    /**
     * HSV 肤色检测
     *
     * 使用 HSV 颜色空间中经典的肤色范围：
     *   - 亚洲/ Caucasian 肤色：H 0~50, S 20~150, V 50~255
     *   - 深色肤色：H 0~50, S 20~255, V 20~200
     *
     * @return 肤色像素占总采样像素的比例 (0~1)
     */
    private double detectSkinColor(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int skinPixels = 0;
        int totalPixels = 0;

        // 采样步长
        int step = Math.max(1, (int) Math.sqrt(w * h / 8000.0));

        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                Color c = new Color(image.getRGB(x, y));
                float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);

                float hue = hsv[0] * 360;        // 0~360
                float sat = hsv[1] * 255;         // 0~255
                float val = hsv[2] * 255;         // 0~255

                // 经典肤色范围（多种肤色适应）
                boolean isSkin = false;

                // 范围 1：亚洲/浅色皮肤
                if (hue >= 0 && hue <= 50 && sat >= 20 && sat <= 150 && val >= 50 && val <= 255) {
                    isSkin = true;
                }
                // 范围 2：深色皮肤（饱和度范围更宽）
                if (hue >= 0 && hue <= 50 && sat >= 20 && sat <= 255 && val >= 20 && val <= 200) {
                    isSkin = true;
                }
                // 范围 3：偏红皮肤（某些光照条件下的肤色）
                if (hue >= 330 && hue <= 360 && sat >= 15 && sat <= 120 && val >= 50 && val <= 255) {
                    isSkin = true;
                }

                // 排除纯黑白和灰阶像素（避免误判）
                if (sat < 10 || val < 10 || val > 245) {
                    isSkin = false;
                }

                if (isSkin) {
                    skinPixels++;
                }
                totalPixels++;
            }
        }

        return totalPixels > 0 ? (double) skinPixels / totalPixels : 0.0;
    }

    /**
     * 边缘密度检测 —— 使用 Sobel 算子
     *
     * 二维码 / 文本图片通常有极高的边缘密度。
     * 正常的活动照片边缘密度通常较低。
     */
    private double computeEdgeDensity(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        int step = Math.max(1, (int) Math.sqrt(w * h / 5000.0));
        int edgePixels = 0;
        int totalPixels = 0;

        // Sobel 核
        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
        int threshold = 80;  // 边缘判定阈值

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
                if (magnitude > threshold) {
                    edgePixels++;
                }
                totalPixels++;
            }
        }

        return totalPixels > 0 ? (double) edgePixels / totalPixels : 0.0;
    }

    /**
     * 检测图像中是否存在大面积单色/纯色区域
     * （用于识别纯色作弊图、闪光灯过曝图等）
     */
    private double detectUniformRegion(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int step = Math.max(1, (int) Math.sqrt(w * h / 4000.0));

        // 将图像划分为 4x4 网格，统计每个网格的颜色方差
        int gridCols = 4;
        int gridRows = 4;
        int cellW = Math.max(1, w / gridCols);
        int cellH = Math.max(1, h / gridRows);

        int uniformCells = 0;
        int totalCells = 0;

        for (int gy = 0; gy < gridRows; gy++) {
            for (int gx = 0; gx < gridCols; gx++) {
                int startX = gx * cellW;
                int startY = gy * cellH;
                int endX = Math.min((gx + 1) * cellW, w);
                int endY = Math.min((gy + 1) * cellH, h);

                long sumR = 0, sumG = 0, sumB = 0;
                int count = 0;

                for (int y = startY; y < endY; y += step) {
                    for (int x = startX; x < endX; x += step) {
                        Color c = new Color(image.getRGB(x, y));
                        sumR += c.getRed();
                        sumG += c.getGreen();
                        sumB += c.getBlue();
                        count++;
                    }
                }

                if (count == 0) continue;

                double avgR = (double) sumR / count;
                double avgG = (double) sumG / count;
                double avgB = (double) sumB / count;

                // 计算该网格内的颜色方差
                double varSum = 0;
                int varCount = 0;
                for (int y = startY; y < endY; y += step) {
                    for (int x = startX; x < endX; x += step) {
                        Color c = new Color(image.getRGB(x, y));
                        double dr = c.getRed() - avgR;
                        double dg = c.getGreen() - avgG;
                        double db = c.getBlue() - avgB;
                        varSum += dr * dr + dg * dg + db * db;
                        varCount++;
                    }
                }

                double variance = varCount > 0 ? varSum / varCount / (255.0 * 255.0 * 3) : 0;
                if (variance < 0.02) {  // 方差极小 -> 该网格近乎纯色
                    uniformCells++;
                }
                totalCells++;
            }
        }

        return totalCells > 0 ? (double) uniformCells / totalCells : 0.0;
    }

    private ModerationResult reject(String reason, double score, List<String> details) {
        return new ModerationResult(false, reason, score, details);
    }
}

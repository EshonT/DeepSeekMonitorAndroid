"""
从 DeepSeekMonitorWindows 的 icon.png (512x512) 生成 Android 启动图标。
生成内容：
  - mipmap 各密度 ic_launcher.png / ic_launcher_round.png（彩色版）
  - mipmap 各密度 ic_launcher_eink.png / ic_launcher_eink_round.png（水墨屏版）
  - drawable 各密度自适应图标前景 PNG（彩色版 + 水墨屏版）
"""
from pathlib import Path
from PIL import Image, ImageOps, ImageDraw

PROJECT_ROOT = Path(__file__).resolve().parent.parent
RES_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res"
SOURCE_ICON = PROJECT_ROOT.parent / "DeepSeekMonitorWindows" / "src-tauri" / "icons" / "icon.png"

# 自适应图标背景色（DeepSeek 品牌蓝）
BG_COLOR = (77, 107, 254, 255)  # #4D6BFE

MIPMAP_SIZES = {
    "mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192,
}

# (canvas_px, safe_zone_px) for each density
FOREGROUND_DP = {
    "mdpi": (108, 72), "hdpi": (162, 108), "xhdpi": (216, 144),
    "xxhdpi": (324, 216), "xxxhdpi": (432, 288),
}


def ensure_dir(path: Path):
    path.mkdir(parents=True, exist_ok=True)


def make_rounded_mask(size: int) -> Image.Image:
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size - 1, size - 1), fill=255)
    return mask


def apply_eink_filter(img: Image.Image) -> Image.Image:
    """
    将图标转换为水墨屏适配版本：
    1. 转为灰度
    2. 提高对比度，接近纯黑白色
    3. 保留 alpha 通道
    """
    # 分离 alpha 通道
    alpha = img.split()[-1] if img.mode == "RGBA" else None

    # 转为灰度
    gray = img.convert("L")

    # 应用对比度增强 + 二值化阈值（> 128 为白，<= 128 为黑）
    # 使用 point 做阈值处理，保留一定中间调给深灰
    def eink_threshold(v):
        if v < 80:
            return 0       # 纯黑
        elif v < 160:
            return 85      # 深灰 (~#555555)
        elif v < 220:
            return 170     # 浅灰 (~#AAAAAA)
        else:
            return 255     # 纯白

    gray_eink = gray.point(eink_threshold)

    # 合并回 RGBA
    if alpha:
        result = Image.merge("RGBA", (gray_eink, gray_eink, gray_eink, alpha))
    else:
        result = gray_eink.convert("RGBA")

    return result


def apply_eink_bg(source: Image.Image) -> Image.Image:
    """
    水墨屏背景：白色圆形 + 黑色细边框，图标前景为高对比度黑白。
    返回带白底圆形背景的完整图标。
    """
    size = source.size[0]
    result = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(result)

    # 白色圆形背景
    margin = size // 16  # 留边距
    draw.ellipse((margin, margin, size - 1 - margin, size - 1 - margin),
                 fill=(255, 255, 255, 255), outline=(0, 0, 0, 255), width=max(1, size // 48))

    # 将 E-Ink 图标贴入圆形内（缩放到 70% 居中）
    inner_size = int(size * 0.70)
    inner = source.resize((inner_size, inner_size), Image.LANCZOS)
    inner_eink = apply_eink_filter(inner)

    offset = (size - inner_size) // 2
    result.paste(inner_eink, (offset, offset), inner_eink)

    return result


def generate_mipmap_icons(source: Image.Image):
    """生成各密度 mipmap 启动图标（标准版 + 水墨屏版）"""
    print("=== generate mipmap icons ===")
    for density, size in MIPMAP_SIZES.items():
        out_dir = RES_DIR / f"mipmap-{density}"
        ensure_dir(out_dir)

        # ── 标准版 ──
        icon = source.resize((size, size), Image.LANCZOS)
        out_path = out_dir / "ic_launcher.png"
        icon.save(out_path, "PNG")
        print(f"  {out_path.relative_to(PROJECT_ROOT)} ({size}x{size})")

        # 标准圆形
        round_icon = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        mask = make_rounded_mask(size)
        round_icon.paste(icon, (0, 0))
        round_icon.putalpha(mask)
        out_path = out_dir / "ic_launcher_round.png"
        round_icon.save(out_path, "PNG")
        print(f"  {out_path.relative_to(PROJECT_ROOT)} ({size}x{size})")

        # ── 水墨屏版（白底黑字高对比） ──
        eink_icon = apply_eink_bg(source)
        eink_icon = eink_icon.resize((size, size), Image.LANCZOS)
        out_path = out_dir / "ic_launcher_eink.png"
        eink_icon.save(out_path, "PNG")
        print(f"  {out_path.relative_to(PROJECT_ROOT)} ({size}x{size})")

        # 水墨屏圆形
        eink_round = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        eink_mask = make_rounded_mask(size)
        eink_round.paste(eink_icon, (0, 0))
        eink_round.putalpha(eink_mask)
        out_path = out_dir / "ic_launcher_eink_round.png"
        eink_round.save(out_path, "PNG")
        print(f"  {out_path.relative_to(PROJECT_ROOT)} ({size}x{size})")


def generate_foreground_icons(source: Image.Image):
    """生成自适应图标前景 PNG（标准版 + 水墨屏版）"""
    print("\n=== generate adaptive icon foregrounds ===")
    for density, (canvas_px, safe_px) in FOREGROUND_DP.items():
        out_dir = RES_DIR / f"drawable-{density}"
        ensure_dir(out_dir)

        # ── 标准版 ──
        foreground = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
        icon = source.resize((safe_px, safe_px), Image.LANCZOS)
        offset = (canvas_px - safe_px) // 2
        foreground.paste(icon, (offset, offset))
        out_path = out_dir / "ic_launcher_foreground.png"
        foreground.save(out_path, "PNG")
        print(f"  {out_path.relative_to(PROJECT_ROOT)} "
              f"(canvas={canvas_px}x{canvas_px}, icon={safe_px}x{safe_px})")

        # ── 水墨屏版：高对比度黑白 ──
        eink_fg = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
        eink_source = apply_eink_filter(source)
        eink_icon = eink_source.resize((safe_px, safe_px), Image.LANCZOS)
        eink_fg.paste(eink_icon, (offset, offset))
        out_path = out_dir / "ic_launcher_foreground_eink.png"
        eink_fg.save(out_path, "PNG")
        print(f"  {out_path.relative_to(PROJECT_ROOT)} "
              f"(canvas={canvas_px}x{canvas_px}, icon={safe_px}x{safe_px})")


def main():
    if not SOURCE_ICON.exists():
        print(f"ERROR: source icon not found: {SOURCE_ICON}")
        return 1

    print(f"Source: {SOURCE_ICON}")
    source = Image.open(SOURCE_ICON).convert("RGBA")
    print(f"Size: {source.size[0]}x{source.size[1]}\n")

    generate_mipmap_icons(source)
    generate_foreground_icons(source)

    # 默认 drawable 目录下的回退文件（标准版 + 水墨屏版）
    src_fg = RES_DIR / "drawable-xxxhdpi" / "ic_launcher_foreground.png"
    dst_fg = RES_DIR / "drawable" / "ic_launcher_foreground.png"
    dst_fg.write_bytes(src_fg.read_bytes())
    print(f"\n  Copied default fallback: {dst_fg.relative_to(PROJECT_ROOT)}")

    src_fg_eink = RES_DIR / "drawable-xxxhdpi" / "ic_launcher_foreground_eink.png"
    dst_fg_eink = RES_DIR / "drawable" / "ic_launcher_foreground_eink.png"
    dst_fg_eink.write_bytes(src_fg_eink.read_bytes())
    print(f"  Copied default fallback: {dst_fg_eink.relative_to(PROJECT_ROOT)}")

    print("\nAll icons generated successfully.")


if __name__ == "__main__":
    exit(main())

#!/usr/bin/env python3
from PIL import Image, ImageDraw
import os

def create_icon(size, output_path):
    # Create image with transparent background
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Draw blue circle background
    margin = size // 8
    draw.ellipse([margin, margin, size-margin, size-margin], 
                fill=(33, 150, 243, 255))
    
    # Draw white triangle (health/medical symbol inspired)
    center = size // 2
    triangle_size = size // 3
    points = [
        (center, center - triangle_size//2),  # top
        (center - triangle_size//2, center + triangle_size//2),  # bottom left
        (center + triangle_size//2, center + triangle_size//2)   # bottom right
    ]
    draw.polygon(points, fill=(255, 255, 255, 255))
    
    # Save the image
    img.save(output_path, 'PNG')
    print(f"Created icon: {output_path} ({size}x{size})")

# Icon sizes for different densities
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

res_base = 'app/src/main/res/'

for folder, size in sizes.items():
    folder_path = os.path.join(res_base, folder)
    if os.path.exists(folder_path):
        # Create both regular and round icons
        create_icon(size, os.path.join(folder_path, 'ic_launcher.png'))
        create_icon(size, os.path.join(folder_path, 'ic_launcher_round.png'))

print("All icons created successfully!")
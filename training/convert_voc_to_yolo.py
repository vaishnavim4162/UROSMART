import os
import xml.etree.ElementTree as ET
import shutil
import random
from pathlib import Path

# Configuration
# Requested Classes: Yeast (0), Triple Phosphate (1), Calcium Oxalate (2), Squamous Cells (3), Uric Acid (4)
CLASSES_MAP = {
    'mycete': 0,    # Yeast
    'cryst': 1,     # Triple Phosphate (Grouped as Crystal placeholder)
    # Calcium Oxalate (2) - No specific data found, currently mapping to Triple Phosphate for now?
    # No, better to keep them separate in names but map the data we have.
    'epith': 3,     # Squamous Cells
}
# We will define the names in data.yaml. Here we just need the mapping.
ORIGINAL_CLASSES = ['eryth', 'leuko', 'mycete', 'epith', 'epithn', 'cryst', 'cast', 'I do not know']

SRC_IMAGES = r"c:\Users\Vishnu\OneDrive\Desktop\taining model\JPEGImages-20260506T174611Z-3-001\JPEGImages"
SRC_ANNOS = r"c:\Users\Vishnu\OneDrive\Desktop\taining model\Annotations-20260506T174623Z-3-001\Annotations"
DST_ROOT = r"c:\Andriodstudios\URO\training\dataset"

TRAIN_RATIO = 0.8

def convert_bbox(size, box):
    dw = 1.0 / size[0]
    dh = 1.0 / size[1]
    x = (box[0] + box[1]) / 2.0
    y = (box[2] + box[3]) / 2.0
    w = box[1] - box[0]
    h = box[3] - box[2]
    return (x * dw, y * dh, w * dw, h * dh)

def convert_annotation(xml_path, txt_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    size = root.find('size')
    w = int(size.find('width').text)
    h = int(size.find('height').text)

    if w == 0 or h == 0:
        return False

    with open(txt_path, 'w') as f:
        for obj in root.iter('object'):
            cls = obj.find('name').text
            if cls not in CLASSES_MAP:
                continue
            cls_id = CLASSES_MAP[cls]
            xmlbox = obj.find('bndbox')
            b = (float(xmlbox.find('xmin').text), float(xmlbox.find('xmax').text),
                 float(xmlbox.find('ymin').text), float(xmlbox.find('ymax').text))
            bb = convert_bbox((w, h), b)
            f.write(f"{cls_id} {' '.join([f'{a:.6f}' for a in bb])}\n")
    return True

def main():
    files = [f.stem for f in Path(SRC_ANNOS).glob("*.xml")]
    random.shuffle(files)
    
    split_idx = int(len(files) * TRAIN_RATIO)
    train_files = files[:split_idx]
    val_files = files[split_idx:]

    print(f"Total files: {len(files)}")
    print(f"Train: {len(train_files)}, Val: {len(val_files)}")

    for subset, file_list in [("train", train_files), ("val", val_files)]:
        img_dst = os.path.join(DST_ROOT, "images", subset)
        lbl_dst = os.path.join(DST_ROOT, "labels", subset)
        os.makedirs(img_dst, exist_ok=True)
        os.makedirs(lbl_dst, exist_ok=True)
        
        for name in file_list:
            xml_path = os.path.join(SRC_ANNOS, f"{name}.xml")
            img_path = os.path.join(SRC_IMAGES, f"{name}.jpg")
            txt_path = os.path.join(lbl_dst, f"{name}.txt")
            
            if not os.path.exists(img_path):
                print(f"Warning: Image not found {img_path}")
                continue
                
            if convert_annotation(xml_path, txt_path):
                shutil.copy(img_path, os.path.join(img_dst, f"{name}.jpg"))

    print("Conversion complete.")

if __name__ == "__main__":
    main()

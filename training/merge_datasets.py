import os
import shutil
from pathlib import Path

# Target classes mapping
# 0: Yeast
# 1: Triple Phosphate
# 2: Calcium Oxalate
# 3: Squamous Cells
# 4: Uric Acid

# Folder 1 Mapping (Source: c:\Users\Vishnu\OneDrive\Desktop\training\1)
# Names: ['acyclovir', 'ammonium biurate', 'bilirubin', 'calcium carbonate', 'calcium oxalate', 'calcium phosphate', 'cholesterol', 'cystein', 'hippuric', 'leucine', 'triple phosphate', 'tyrosine', 'uric acid']
F1_MAP = {
    4: 2,   # calcium oxalate -> Calcium Oxalate
    10: 1,  # triple phosphate -> Triple Phosphate
    12: 4   # uric acid -> Uric Acid
}

# Folder 2 Mapping (Source: c:\Users\Vishnu\OneDrive\Desktop\training\2)
# Names: ['0', '1', '2', '3', '4', '5', 'crystals']
F2_MAP = {
    0: 0,   # Yeast
    1: 1,   # Triple Phosphate
    2: 2,   # Calcium Oxalate
    3: 3,   # Squamous Cells
    4: 4    # Uric Acid
}

SRC_F1 = r"c:\Users\Vishnu\OneDrive\Desktop\training\1"
SRC_F2 = r"c:\Users\Vishnu\OneDrive\Desktop\training\2"
DST_ROOT = r"c:\Andriodstudios\URO\training\dataset"

def process_subset(src_dir, mapping, subset_name, prefix):
    # Subsets in Roboflow: train, valid, test
    # Target subsets in DST: train, val
    
    # Map subset names
    dst_subset = "train" if subset_name == "train" else "val"
    
    img_src = os.path.join(src_dir, subset_name, "images")
    lbl_src = os.path.join(src_dir, subset_name, "labels")
    
    img_dst = os.path.join(DST_ROOT, "images", dst_subset)
    lbl_dst = os.path.join(DST_ROOT, "labels", dst_subset)
    
    os.makedirs(img_dst, exist_ok=True)
    os.makedirs(lbl_dst, exist_ok=True)
    
    if not os.path.exists(img_src):
        print(f"Warning: Subset {subset_name} images not found in {src_dir}")
        return

    count = 0
    for img_file in os.listdir(img_src):
        if not img_file.lower().endswith(('.jpg', '.jpeg', '.png')):
            continue
            
        base_name = os.path.splitext(img_file)[0]
        lbl_file = f"{base_name}.txt"
        lbl_path = os.path.join(lbl_src, lbl_file)
        
        if not os.path.exists(lbl_path):
            continue
            
        # Read and filter labels
        new_labels = []
        with open(lbl_path, "r") as f:
            for line in f:
                parts = line.strip().split()
                if not parts:
                    continue
                cls_id = int(parts[0])
                if cls_id in mapping:
                    target_id = mapping[cls_id]
                    new_labels.append(f"{target_id} {' '.join(parts[1:])}")
        
        # If any labels match our target classes, copy image and write new label
        if new_labels:
            dst_name = f"{prefix}_{base_name}"
            # Copy image
            shutil.copy(os.path.join(img_src, img_file), os.path.join(img_dst, f"{dst_name}.jpg"))
            # Write labels
            with open(os.path.join(lbl_dst, f"{dst_name}.txt"), "w") as f:
                f.write("\n".join(new_labels) + "\n")
            count += 1
            
    print(f"Processed {count} images from {src_dir} ({subset_name}) -> {dst_subset}")

def main():
    print("Starting merge process...")
    
    # Process Folder 1
    process_subset(SRC_F1, F1_MAP, "train", "f1")
    process_subset(SRC_F1, F1_MAP, "valid", "f1") # Map valid to val
    process_subset(SRC_F1, F1_MAP, "test", "f1")  # Map test to val (optional, but good for more data)
    
    # Process Folder 2
    process_subset(SRC_F2, F2_MAP, "train", "f2")
    process_subset(SRC_F2, F2_MAP, "valid", "f2")
    process_subset(SRC_F2, F2_MAP, "test", "f2")
    
    print("\nMerge complete.")

if __name__ == "__main__":
    main()

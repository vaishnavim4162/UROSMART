from ultralytics import YOLO
import os
import shutil

def main():
    # Paths
    weights_path = r"c:\Andriodstudios\URO\training\runs\detect\train_uro_specific\weights\best.pt"
    export_dir = r"c:\Andriodstudios\URO\training\deployment"
    
    if not os.path.exists(export_dir):
        os.makedirs(export_dir)
    
    print(f"Loading best model from {weights_path}...")
    model = YOLO(weights_path)
    
    # 1. Export to ONNX
    print("Exporting model to ONNX format...")
    # opset=12 is standard for mobile compatibility
    onnx_path = model.export(format='onnx', imgsz=320, opset=12)
    print(f"Export complete: {onnx_path}")
    
    # 2. Archive results
    print("Archiving final weights and assets to deployment folder...")
    
    # Copy best.pt
    shutil.copy2(weights_path, os.path.join(export_dir, "urosmart_best.pt"))
    
    # Copy ONNX
    if os.path.exists(onnx_path):
        shutil.copy2(onnx_path, os.path.join(export_dir, "urosmart_model.onnx"))
        print(f"Copied ONNX model to {export_dir}")
        
        # Also copy directly to Android Assets
        android_assets = r"c:\Andriodstudios\URO\app\src\main\assets"
        if os.path.exists(android_assets):
            shutil.copy2(onnx_path, os.path.join(android_assets, "urosmart_model.onnx"))
            print(f"Directly updated Android app assets at: {android_assets}")

    # Copy plots if they exist
    plots_dir = r"c:\Andriodstudios\URO\training\runs\detect\train_uro_specific"
    for item in os.listdir(plots_dir):
        if item.endswith(".png") or item.endswith(".jpg") or item == "results.csv":
            shutil.copy2(os.path.join(plots_dir, item), os.path.join(export_dir, item))

    print(f"\nFinal deployment assets (ONNX) are ready in: {export_dir}")

if __name__ == "__main__":
    main()

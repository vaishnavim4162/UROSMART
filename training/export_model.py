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
    
    # 1. Export to TFLite
    print("Exporting model to TFLite format...")
    # imgsz=320 to match training and ensure speed on mobile
    tflite_path = model.export(format='tflite', imgsz=320)
    print(f"Export complete: {tflite_path}")
    
    # 2. Run final validation to generate plots
    print("Running final validation to generate clinical metrics...")
    metrics = model.val(data=r"c:\Andriodstudios\URO\training\data.yaml", imgsz=320)
    
    # 3. Archive results
    print("Archiving final weights and assets to deployment folder...")
    
    # Copy best.pt
    shutil.copy2(weights_path, os.path.join(export_dir, "urosmart_best.pt"))
    
    # Move tflite (it's usually in a folder next to weights)
    # YOLOv8 export usually creates a folder like 'best_saved_model' or just 'best_float32.tflite'
    # We'll look for it.
    base_name = os.path.splitext(os.path.basename(weights_path))[0]
    expected_tflite = weights_path.replace(".pt", "_saved_model")
    if os.path.exists(expected_tflite):
        shutil.copytree(expected_tflite, os.path.join(export_dir, "tflite_model"), dirs_exist_ok=True)
    
    # Also look for single file tflite
    single_tflite = weights_path.replace(".pt", "_float32.tflite")
    if not os.path.exists(single_tflite):
        single_tflite = weights_path.replace(".pt", ".tflite") # fallback
        
    if os.path.exists(single_tflite):
        shutil.copy2(single_tflite, os.path.join(export_dir, "urosmart_model.tflite"))
        print(f"Copied TFLite model to {export_dir}")
        
        # Also copy directly to Android Assets
        android_assets = r"c:\Andriodstudios\URO\app\src\main\assets"
        if os.path.exists(android_assets):
            shutil.copy2(single_tflite, os.path.join(android_assets, "urosmart_model.tflite"))
            print(f"Directly updated Android app assets at: {android_assets}")

    # Copy plots
    plots_dir = r"c:\Andriodstudios\URO\training\runs\detect\train_uro_specific"
    for item in os.listdir(plots_dir):
        if item.endswith(".png") or item.endswith(".jpg") or item == "results.csv":
            shutil.copy2(os.path.join(plots_dir, item), os.path.join(export_dir, item))

    print(f"\nFinal deployment assets are ready in: {export_dir}")

if __name__ == "__main__":
    main()

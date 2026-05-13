from ultralytics import YOLO
import os

def main():
    # Load a model
    model = YOLO("yolov8n.pt")  # load a pretrained model

    # Train the model
    # We use a small image size and batch size for CPU compatibility if needed
    results = model.train(
        data=r"c:\Andriodstudios\URO\training\data.yaml",
        epochs=10,
        imgsz=640,
        plots=True,
        device='cpu' # Force CPU if no GPU found, change to '0' for GPU
    )

    # Export the model to TFLite format for Android
    print("Exporting model to TFLite...")
    model.export(format="tflite", imgsz=640)
    
    print("Training and Export complete.")

if __name__ == "__main__":
    main()

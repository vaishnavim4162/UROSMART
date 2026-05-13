from ultralytics import YOLO
import os

def get_current_epoch():
    if os.path.exists("current_epoch.txt"):
        with open("current_epoch.txt", "r") as f:
            try:
                return int(f.read().strip())
            except:
                return 0
    return 0

def save_current_epoch(epoch):
    with open("current_epoch.txt", "w") as f:
        f.write(str(epoch))

def main():
    current_epoch = get_current_epoch()
    target_epoch = current_epoch + 1
    
    if target_epoch > 10:
        print(f"Already completed 10 epochs. Current epoch: {current_epoch}")
        return

    print(f"Starting Epoch {target_epoch} out of 10...")
    
    # Path to data.yaml
    data_path = r"c:\Andriodstudios\URO\training\data.yaml"
    
    # Load model
    if current_epoch == 0:
        model = YOLO("yolov8n.pt")
        resume = False
    else:
        # Load the last checkpoint
        last_weights = r"c:\Andriodstudios\URO\training\runs\detect\train_uro_specific\weights\last.pt"
        if os.path.exists(last_weights):
            model = YOLO(last_weights)
            resume = True
        else:
            print(f"Checkpoint not found at {last_weights}. Starting from scratch.")
            model = YOLO("yolov8n.pt")
            resume = False

    # Train for exactly one more epoch
    # Note: In YOLOv8, 'epochs' is the TOTAL number of epochs to train for.
    # So to run the next epoch, we set epochs to target_epoch.
    model.train(
        data=data_path,
        epochs=target_epoch,
        imgsz=320,  # Reduced from 640 for speed on CPU
        device='cpu',
        name='train_uro_specific',
        exist_ok=True,
        resume=resume
    )
    
    save_current_epoch(target_epoch)
    print(f"\nSuccessfully completed Epoch {target_epoch}/10.")
    print("Stopping as requested. Please say 'continue' to move to the next epoch.")

if __name__ == "__main__":
    main()

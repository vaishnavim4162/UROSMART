import subprocess
import os

def get_current_epoch():
    if os.path.exists("current_epoch.txt"):
        with open("current_epoch.txt", "r") as f:
            try:
                return int(f.read().strip())
            except:
                return 0
    return 0

def main():
    target_stop_epoch = 4
    
    while True:
        current = get_current_epoch()
        if current >= target_stop_epoch:
            print(f"Reached target epoch {target_stop_epoch}. Stopping.")
            break
        
        print(f"\n--- Starting Epoch {current + 1} ---")
        # Run the existing train_one_epoch script
        result = subprocess.run(["python", "train_one_epoch.py"], capture_output=False)
        
        if result.returncode != 0:
            print("Training failed. Stopping.")
            break

if __name__ == "__main__":
    main()

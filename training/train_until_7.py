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
    target_stop_epoch = 7
    
    print(f"Current Epoch: {get_current_epoch()}")
    print(f"Goal: Continue until Epoch {target_stop_epoch}")
    
    while True:
        current = get_current_epoch()
        if current >= target_stop_epoch:
            print(f"\nReached target epoch {target_stop_epoch}. Stopping.")
            break
        
        print(f"\n" + "="*40)
        print(f"--- Starting Session Epoch {current + 1} ---")
        print("="*40)
        
        # Run the existing train_one_epoch script
        result = subprocess.run(["python", "train_one_epoch.py"], capture_output=False)
        
        if result.returncode != 0:
            print("\n[!] Training failed or was interrupted. Stopping.")
            break
        
        # Small delay between epochs
        import time
        time.sleep(2)

if __name__ == "__main__":
    main()

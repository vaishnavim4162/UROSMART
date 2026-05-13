import requests

def download_file(url, local_filename):
    with requests.get(url, stream=True) as r:
        r.raise_for_status()
        with open(local_filename, 'wb') as f:
            for chunk in r.iter_content(chunk_size=8192):
                f.write(chunk)
    return local_filename

try:
    print("Downloading YOLOv11n...")
    download_file("https://github.com/ultralytics/assets/releases/download/v8.3.0/yolov11n.pt", "yolov11n.pt")
    print("Success.")
except Exception as e:
    print(f"Failed: {e}")

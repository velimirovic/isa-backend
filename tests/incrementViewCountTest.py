import requests
from concurrent.futures import ThreadPoolExecutor, as_completed
import time

BASE_URL = "http://localhost:8080/api/video-posts"
VIDEO_DRAFT_ID = "8888389c-5664-417d-82b8-9af5ea391c31"

NUMBER_OF_REQUESTS = 100
MAX_WORKERS = 10
TIMEOUT = 10


def send_request(idx: int):
    try:
        resp = requests.get(
            f"{BASE_URL}/{VIDEO_DRAFT_ID}",
            timeout=TIMEOUT
        )

        snippet = resp.text[:200]
        print(f"[{idx}] {resp.status_code}: {snippet}")

        return resp.status_code

    except requests.Timeout:
        print(f"[{idx}] Timeout")
        return None

    except Exception as e:
        print(f"[{idx}] Error: {e}")
        return None
    
def initial_viewcount():
    try:
        resp = requests.get(
            f"{BASE_URL}/{VIDEO_DRAFT_ID}",
            timeout=TIMEOUT
        )
        
        return resp.json().get("viewCount")

    except requests.Timeout:
        print(f"Timeout")
        return None

    except Exception as e:
        print(f"Error: {e}")
        return None


def main():
    print(f"Starting concurrency test with {NUMBER_OF_REQUESTS} requests...")
    start_time = time.time()

    results = []
    expected_viewcount =  initial_viewcount() + NUMBER_OF_REQUESTS + 1

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        futures = {
            executor.submit(send_request, i + 1): i + 1
            for i in range(NUMBER_OF_REQUESTS)
        }

        for future in as_completed(futures):
            results.append(future.result())

    duration = time.time() - start_time

    print("---------------------------------------")
    print(f"Finished in {duration:.2f} seconds")

    success_count = results.count(200)
    print(f"Successful requests: {success_count}/{NUMBER_OF_REQUESTS}")

    final_resp = requests.get(f"{BASE_URL}/{VIDEO_DRAFT_ID}", timeout=TIMEOUT)
    final_view_count = final_resp.json().get("viewCount")

    print(f"Final viewCount: {final_view_count}")
    print(f"Expected viewCount: {expected_viewcount}")

    assert final_view_count == expected_viewcount, (
        f"Concurrency bug! Expected {expected_viewcount}, got {final_view_count}"
    )


if __name__ == "__main__":
    main()
    #initial_viewcount()

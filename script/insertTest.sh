#!/bin/bash

# Script to populate Elasticsearch index 'prova' with 50 sample documents

for i in {1..50}; do
  curl -X POST "localhost:9200/prova/_doc/$i?pretty" -H 'Content-Type: application/json' -d'

{
  "title": "'"$(case $i in
      1) echo "This pc is broken";;
      2) echo "This tablet is fine";;
      3) echo "Smartphone overheating issues";;
      4) echo "Gaming laptop performance review";;
      5) echo "Tablet battery life comparison";;
      6) echo "Wireless earbuds sound quality";;
      7) echo "PC cooling solutions";;
      8) echo "Smartphone camera comparison";;
      9) echo "Tablet app compatibility";;
      10) echo "Laptop keyboard durability";;
      11) echo "Best PC monitors 2025";;
      12) echo "Tablet accessories review";;
      13) echo "Smartwatch connectivity issues";;
      14) echo "Gaming console performance";;
      15) echo "PC software installation errors";;
      16) echo "Tablet screen resolution trends";;
      17) echo "Laptop battery replacement guide";;
      18) echo "Smartphone app crashes";;
      19) echo "High-end gaming PC builds";;
      20) echo "Tablet weight comparison";;
      21) echo "Laptop cooling pads";;
      22) echo "Smartphone OS update tips";;
      23) echo "PC storage solutions";;
      24) echo "Tablet productivity apps";;
      25) echo "Wireless mouse review";;
      26) echo "Gaming keyboard RGB effects";;
      27) echo "Laptop screen calibration";;
      28) echo "Smartphone accessory compatibility";;
      29) echo "Tablet software updates";;
      30) echo "High-refresh-rate monitors";;
      31) echo "PC malware protection";;
      32) echo "Tablet stylus precision";;
      33) echo "Laptop upgrade tips";;
      34) echo "Smartphone wireless charging";;
      35) echo "PC build mistakes to avoid";;
      36) echo "Tablet audio output";;
      37) echo "Gaming headset comfort";;
      38) echo "Laptop webcam quality";;
      39) echo "Smartphone durability tests";;
      40) echo "PC graphics card comparison";;
      41) echo "Tablet weight vs. performance";;
      42) echo "Laptop sound quality";;
      43) echo "Smartphone app security";;
      44) echo "PC motherboard features";;
      45) echo "Tablet firmware updates";;
      46) echo "Wireless router speed tests";;
      47) echo "Laptop thermal management";;
      48) echo "Smartphone storage options";;
      49) echo "Tablet multitasking performance";;
      50) echo "PC peripheral compatibility";;
  esac)"'",
  "url": "www.example'"$i"'.com",
  "main_content": "'"$(case $i in
      1) echo "This is a ia generated document and shows how pc performs.";;
      2) echo "This is a sample document for testing how tablet and technology works.";;
      3) echo "Users report their devices heat up during heavy app usage, affecting performance.";;
      4) echo "A detailed review of FPS benchmarks and thermal performance under load.";;
      5) echo "This document compares battery duration across several tablet models.";;
      6) echo "An in-depth look at frequency response, noise cancellation, and comfort.";;
      7) echo "Exploring liquid and air cooling options to optimize desktop performance.";;
      8) echo "A photo shoot test to evaluate camera performance under different lighting conditions.";;
      9) echo "Analysis of which apps run smoothly on different tablet OS versions.";;
      10) echo "Testing mechanical and membrane keyboards for long-term use.";;
      11) echo "A guide covering resolution, refresh rate, and color accuracy for various monitors.";;
      12) echo "Comparing styluses, covers, and docks for productivity and protection.";;
      13) echo "Common problems syncing with Android and iOS devices and potential fixes.";;
      14) echo "FPS and load time tests across the latest consoles to find the top performer.";;
      15) echo "Common software issues, troubleshooting steps, and best practices for installations.";;
      16) echo "Examining the shift from HD to 4K screens in modern tablets.";;
      17) echo "Step-by-step instructions on replacing old batteries safely.";;
      18) echo "Investigating why certain apps fail and how OS updates impact stability.";;
      19) echo "Assembling components for maximum graphics performance and minimal latency.";;
      20) echo "Which tablets are light enough for travel yet maintain large screen usability.";;
      21) echo "Evaluating passive and active cooling pads to reduce thermal throttling.";;
      22) echo "How to update safely without losing data or causing slowdowns.";;
      23) echo "Comparing SSD, NVMe, and HDD options for speed and capacity.";;
      24) echo "Exploring note-taking, design, and office apps for efficient workflow.";;
      25) echo "Testing latency, battery life, and ergonomics of popular wireless mice.";;
      26) echo "Showcasing lighting modes and customizability in gaming setups.";;
      27) echo "How to adjust brightness, contrast, and color profiles for accurate display.";;
      28) echo "Testing cases, chargers, and docks to ensure proper fit and functionality.";;
      29) echo "Impact of OS updates on performance, battery, and app compatibility.";;
      30) echo "Benefits of 120Hz, 144Hz, and 240Hz panels for gamers and designers.";;
      31) echo "Best practices for antivirus software, firewalls, and safe browsing.";;
      32) echo "Testing pen pressure sensitivity and lag for drawing applications.";;
      33) echo "How to safely upgrade RAM, storage, and GPU components.";;
      34) echo "Examining charging speed, efficiency, and compatibility across devices.";;
      35) echo "Common errors in assembling custom PCs and how to prevent them.";;
      36) echo "Comparing speaker quality, volume, and headphone support.";;
      37) echo "Testing long-duration wear and noise isolation for immersive gaming.";;
      38) echo "Assessing image clarity, low-light performance, and software enhancements.";;
      39) echo "Drop, scratch, and water-resistance tests to measure build quality.";;
      40) echo "Benchmarking GPUs for gaming, rendering, and AI workloads.";;
      41) echo "Finding a balance between portability and processing power for tablets.";;
      42) echo "Examining built-in speakers and headphone output performance.";;
      43) echo "Analyzing permissions, malware risk, and best security practices.";;
      44) echo "Guide to choosing boards with the right slots, ports, and expansion options.";;
      45) echo "How firmware upgrades can improve stability, security, and performance.";;
      46) echo "Testing throughput and coverage in real home environments.";;
      47) echo "Tips to maintain optimal temperature and prevent throttling.";;
      48) echo "Comparing internal memory, SD card support, and cloud storage alternatives.";;
      49) echo "Assessing how well different models handle multiple apps and split-screen features.";;
      50) echo "Ensuring keyboards, mice, and external drives work seamlessly across OS versions.";;
  esac)"'"
}
'
done


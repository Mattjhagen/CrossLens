const { spawn } = require('child_process');
const fs = require('fs');

// Simple screenshot capture using AppleScript and screencapture
async function captureScreenshot(filename, scrollY = 0) {
  return new Promise((resolve) => {
    // Use JavaScript to scroll
    const script = `
      tell application "Google Chrome"
        tell the active tab of its first window
          execute javascript "window.scrollTo({top: ${scrollY}, behavior: 'instant'});"
        end tell
      end tell
    `;

    const osascript = spawn('osascript', ['-e', script]);

    osascript.on('close', () => {
      // Wait for scroll to complete
      setTimeout(() => {
        const capture = spawn('screencapture', ['-x', '-w', filename]);
        capture.on('close', () => {
          console.log(`Captured: ${filename}`);
          resolve();
        });
      }, 500);
    });
  });
}

async function main() {
  console.log('Starting screenshot capture...');
  console.log('Click on the Chrome window when prompted for each screenshot.');

  // Hero at top
  await new Promise(r => setTimeout(r, 2000));
  await captureScreenshot('/tmp/hero-top.png', 0);

  // Hero mid-scroll
  await new Promise(r => setTimeout(r, 2000));
  await captureScreenshot('/tmp/hero-mid.png', 800);

  // Settings section (scroll further down)
  await new Promise(r => setTimeout(r, 2000));
  await captureScreenshot('/tmp/settings-section.png', 4000);

  // Back to top for mobile test
  await new Promise(r => setTimeout(r, 2000));

  console.log('Screenshots captured to /tmp/');
  console.log('hero-top.png, hero-mid.png, settings-section.png');
}

main().catch(console.error);

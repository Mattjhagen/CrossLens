// Theme Toggle
function toggleTheme() {
    const html = document.documentElement;
    if (html.getAttribute('data-theme') === 'light') {
        html.removeAttribute('data-theme');
        localStorage.setItem('theme', 'dark');
    } else {
        html.setAttribute('data-theme', 'light');
        localStorage.setItem('theme', 'light');
    }
}

// Check for reduced motion preference
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

// Utility: clamp value between min and max
function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
}

// Utility: smooth easing function
function easeOutCubic(t) {
    return 1 - Math.pow(1 - t, 3);
}

// Hero 3D Rotation Animation (desktop only, not on mobile or reduced-motion)
function initHeroAnimation() {
    if (prefersReducedMotion || window.innerWidth < 721) {
        return; // Skip animation on mobile or if reduced motion preferred
    }

    const heroWrap = document.querySelector('.hero-sticky-wrap');
    const heroSticky = document.querySelector('.hero-sticky');
    const heroDevice = document.querySelector('.hero-device-phone');
    const heroText = document.querySelector('.hero-text-col');

    if (!heroWrap || !heroSticky || !heroDevice) return;

    function updateHeroAnimation() {
        const wrapRect = heroWrap.getBoundingClientRect();
        const wrapHeight = heroWrap.offsetHeight;
        const viewportHeight = window.innerHeight;

        // Calculate scroll progress through the hero wrap (0 to 1)
        const scrollProgress = clamp(
            (viewportHeight - wrapRect.top) / (wrapHeight - viewportHeight),
            0,
            1
        );

        // Phase 1: Initial entrance (0-0.15)
        // Phase 2: Hold and subtle rotate (0.15-0.6)
        // Phase 3: Text fade out (0.6-1)

        // Text fade based on scroll
        if (scrollProgress < 0.6) {
            heroText.style.opacity = '1';
            heroText.style.transform = 'translateY(0)';
        } else {
            const fadeProgress = (scrollProgress - 0.6) / 0.4;
            const fadeEased = easeOutCubic(fadeProgress);
            heroText.style.opacity = String(1 - fadeEased * 0.95);
            heroText.style.transform = `translateY(${-fadeEased * 20}px)`;
        }

        // Phone 3D rotation (subtle, editorial)
        if (scrollProgress > 0.15 && scrollProgress < 0.75) {
            const rotateProgress = (scrollProgress - 0.15) / 0.6;
            const rotateEased = Math.sin(rotateProgress * Math.PI); // Peak in middle

            // Subtle rotation: max 8deg Y-axis, 3deg X-axis
            const rotateY = rotateEased * 8;
            const rotateX = rotateEased * 3;
            const scale = 1 + (rotateEased * 0.05); // Slight scale up

            heroDevice.style.transform = `
                perspective(1200px)
                rotateY(${rotateY}deg)
                rotateX(${-rotateX}deg)
                scale(${scale})
                translateZ(${rotateEased * 20}px)
            `;
        } else if (scrollProgress >= 0.75) {
            // Return to front-facing as next section arrives
            const returnProgress = (scrollProgress - 0.75) / 0.25;
            const returnEased = easeOutCubic(returnProgress);
            const rotateY = 8 * (1 - returnEased);
            const rotateX = 3 * (1 - returnEased);
            const scale = 1.05 - (returnEased * 0.05);

            heroDevice.style.transform = `
                perspective(1200px)
                rotateY(${rotateY}deg)
                rotateX(${-rotateX}deg)
                scale(${scale})
            `;
        } else {
            heroDevice.style.transform = 'none';
        }

        // Add 'deep' class when text should fade
        if (scrollProgress > 0.6) {
            heroSticky.classList.add('deep');
        } else {
            heroSticky.classList.remove('deep');
        }
    }

    window.addEventListener('scroll', () => {
        requestAnimationFrame(updateHeroAnimation);
    }, { passive: true });

    // Initial state
    updateHeroAnimation();
}

// Scroll-reveal for sections
function initScrollReveal() {
    const revealElements = document.querySelectorAll('.reveal, .pop-card, .caption-block');

    if (prefersReducedMotion) {
        // Show everything immediately
        revealElements.forEach(el => {
            el.classList.add('visible');
        });
        return;
    }

    const observerOptions = {
        root: null,
        rootMargin: '-10% 0px',
        threshold: 0.1
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, observerOptions);

    revealElements.forEach(el => observer.observe(el));
}

// Pinned device image swap
function initPinnedImageSwap() {
    const pinnedImg = document.getElementById('pinned-img');
    const captionBlocks = document.querySelectorAll('.caption-block[data-pinned-img]');

    if (!pinnedImg || !captionBlocks.length) return;

    const observerOptions = {
        root: null,
        rootMargin: '-40% 0px',
        threshold: 0
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const newSrc = entry.target.getAttribute('data-pinned-img');
                if (newSrc && pinnedImg.src !== newSrc) {
                    pinnedImg.style.opacity = '0.5';
                    setTimeout(() => {
                        pinnedImg.src = newSrc;
                        pinnedImg.style.opacity = '1';
                    }, 150);
                }
            }
        });
    }, observerOptions);

    captionBlocks.forEach(block => observer.observe(block));
}

// Gallery keyboard navigation
function initGalleryNavigation() {
    const scroller = document.querySelector('.gallery-scroller');
    const slides = document.querySelectorAll('.gallery-slide');
    const dots = document.querySelectorAll('.gallery-dot');

    if (!scroller || !slides.length) return;

    // Update active dot based on scroll position
    function updateActiveDot() {
        const scrollLeft = scroller.scrollLeft;
        const slideWidth = slides[0].offsetWidth + 32; // width + gap
        const activeIndex = Math.round(scrollLeft / slideWidth);

        dots.forEach((dot, i) => {
            dot.classList.toggle('active', i === activeIndex);
        });
    }

    scroller.addEventListener('scroll', updateActiveDot, { passive: true });

    // Dot click navigation
    dots.forEach((dot, index) => {
        dot.addEventListener('click', () => {
            const slideWidth = slides[0].offsetWidth + 32;
            scroller.scrollTo({
                left: slideWidth * index,
                behavior: 'smooth'
            });
        });
    });

    // Initial state
    updateActiveDot();
}

// Hero entrance animation
function initHeroEntrance() {
    const heroSticky = document.getElementById('hero-sticky');
    if (!heroSticky) return;

    // Trigger entrance after a brief delay
    setTimeout(() => {
        heroSticky.classList.add('entered');
    }, 100);
}

// Initialize all animations on load
document.addEventListener('DOMContentLoaded', () => {
    initHeroEntrance();
    initHeroAnimation();
    initScrollReveal();
    initPinnedImageSwap();
    initGalleryNavigation();
});

// Re-initialize on resize (debounced)
let resizeTimeout;
window.addEventListener('resize', () => {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(() => {
        // Re-init only hero animation on resize
        initHeroAnimation();
    }, 250);
});

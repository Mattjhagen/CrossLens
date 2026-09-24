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

// Hero Device Rotation (desktop only)
function initHeroAnimation() {
    if (prefersReducedMotion || window.innerWidth < 721) {
        return;
    }

    const heroWrap = document.querySelector('.hero-sticky-wrap');
    const heroSticky = document.querySelector('.hero-sticky');
    const heroDevice = document.querySelector('.hero-device .android-device');
    const heroText = document.querySelector('.hero-text-col');

    if (!heroWrap || !heroSticky || !heroDevice) return;

    function updateHeroAnimation() {
        const wrapRect = heroWrap.getBoundingClientRect();
        const wrapHeight = heroWrap.offsetHeight;
        const viewportHeight = window.innerHeight;

        // Calculate scroll progress (0 to 1)
        const scrollProgress = clamp(
            (viewportHeight - wrapRect.top) / (wrapHeight + viewportHeight * 0.3),
            0,
            1
        );

        // Text fades only after scrolling well past reading area
        if (scrollProgress < 0.75) {
            heroText.style.opacity = '1';
            heroText.style.transform = 'translateY(0)';
        } else {
            const fadeProgress = (scrollProgress - 0.75) / 0.25;
            const fadeEased = easeOutCubic(fadeProgress);
            heroText.style.opacity = String(1 - fadeEased * 0.92);
            heroText.style.transform = `translateY(${-fadeEased * 12}px)`;
        }

        // Hero device on right: clockwise rotation
        const rotateZ = scrollProgress * 2.8; // +2.8deg clockwise
        const rotateY = scrollProgress * -5.2; // -5.2deg Y-axis
        const rotateX = scrollProgress * 0.8; // +0.8deg X-axis
        const scale = 1 - scrollProgress * 0.04; // 1.0 → 0.96

        heroDevice.style.transform = `
            perspective(1500px)
            rotateZ(${rotateZ}deg)
            rotateY(${rotateY}deg)
            rotateX(${rotateX}deg)
            scale(${scale})
        `;

        // Add 'deep' class when text should fade
        if (scrollProgress > 0.75) {
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

// Story Section Device Rotations (desktop only)
function initStoryDeviceRotations() {
    if (prefersReducedMotion || window.innerWidth < 721) {
        return;
    }

    const devices = document.querySelectorAll('.story-section .android-device[data-position]');

    function updateDeviceRotations() {
        devices.forEach(device => {
            const rect = device.getBoundingClientRect();
            const viewportHeight = window.innerHeight;
            const deviceCenter = rect.top + rect.height / 2;

            // Calculate progress as device moves through viewport
            // 0 = entering from bottom, 0.5 = centered, 1 = exiting top
            const viewportProgress = clamp(
                1 - ((deviceCenter - viewportHeight * 0.2) / (viewportHeight * 0.6)),
                0,
                1
            );

            // Smooth easing for natural feel
            const eased = Math.sin(viewportProgress * Math.PI * 0.5);

            const position = device.getAttribute('data-position');

            if (position === 'left') {
                // Left side: counterclockwise rotation
                const rotateZ = eased * -2.4; // -2.4deg counterclockwise
                const rotateY = eased * 5.6; // +5.6deg toward viewer
                const rotateX = eased * -0.6; // -0.6deg X-axis
                const scale = 0.96 + eased * 0.04; // 0.96 → 1.0
                const translateY = (1 - eased) * 8; // 8px → 0

                device.style.transform = `
                    perspective(1500px)
                    rotateZ(${rotateZ}deg)
                    rotateY(${rotateY}deg)
                    rotateX(${rotateX}deg)
                    scale(${scale})
                    translateY(${translateY}px)
                `;
            } else {
                // Right side: clockwise rotation
                const rotateZ = eased * 2.4; // +2.4deg clockwise
                const rotateY = eased * -5.6; // -5.6deg toward viewer
                const rotateX = eased * 0.6; // +0.6deg X-axis
                const scale = 0.96 + eased * 0.04; // 0.96 → 1.0
                const translateY = (1 - eased) * 8; // 8px → 0

                device.style.transform = `
                    perspective(1500px)
                    rotateZ(${rotateZ}deg)
                    rotateY(${rotateY}deg)
                    rotateX(${rotateX}deg)
                    scale(${scale})
                    translateY(${translateY}px)
                `;
            }
        });
    }

    window.addEventListener('scroll', () => {
        requestAnimationFrame(updateDeviceRotations);
    }, { passive: true });

    // Initial state
    updateDeviceRotations();
}

// Scroll-reveal for sections
function initScrollReveal() {
    const revealElements = document.querySelectorAll('.reveal, .pop-card');

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

// Gallery horizontal scroll navigation
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
    initStoryDeviceRotations();
    initScrollReveal();
    initGalleryNavigation();
});

// Re-initialize on resize (debounced)
let resizeTimeout;
window.addEventListener('resize', () => {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(() => {
        initHeroAnimation();
        initStoryDeviceRotations();
    }, 250);
});


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

// 3D Scroll Animations
const updateScrollAnimations = () => {
    const wh = window.innerHeight;
    
    document.querySelectorAll('.scroll-3d-card').forEach(el => {
        const rect = el.getBoundingClientRect();
        const entryPoint = wh - (rect.top + rect.height * 0.2);
        const progress = Math.max(0, Math.min(1, entryPoint / (wh * 0.3)));
        
        const rotate = 60 * (1 - progress);
        const scale = 0.8 + (0.2 * progress);
        const y = 150 * (1 - progress);
        const opacity = progress;
        
        el.style.transform = `translateY(${y}px) scale(${scale}) rotateX(${rotate}deg)`;
        el.style.opacity = opacity;
    });

    document.querySelectorAll('.scroll-3d-text').forEach(el => {
        const rect = el.getBoundingClientRect();
        const delay = parseFloat(el.style.getPropertyValue('--delay') || 0) * 100;
        const entryPoint = wh - rect.top - delay;
        const progress = Math.max(0, Math.min(1, entryPoint / (wh * 0.2)));
        
        const y = 60 * (1 - progress);
        const opacity = progress;
        
        el.style.transform = `translateY(${y}px)`;
        el.style.opacity = opacity;
    });
    
    document.querySelectorAll('.scroll-3d-scale').forEach(el => {
        const rect = el.getBoundingClientRect();
        const elCenter = rect.top + rect.height / 2;
        const screenCenter = wh / 2;
        const dist = Math.abs(elCenter - screenCenter) / (wh * 0.7);
        const progress = Math.max(0, 1 - dist);
        
        const scale = 0.8 + (0.2 * progress);
        const opacity = 0.2 + (0.8 * progress);
        const letterSpacing = -0.05 + (0.02 * (1-progress));
        
        el.style.transform = `scale(${scale})`;
        el.style.opacity = opacity;
        el.style.letterSpacing = `${letterSpacing}em`;
    });
};

window.addEventListener('scroll', () => {
    requestAnimationFrame(updateScrollAnimations);
}, { passive: true });

// Initial trigger
document.addEventListener('DOMContentLoaded', updateScrollAnimations);

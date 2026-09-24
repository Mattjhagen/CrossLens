// CrossLens site interactions and scroll motion.
const reducedMotionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
const desktopQuery = window.matchMedia('(min-width: 721px)');

function toggleTheme() {
  const html = document.documentElement;
  const isLight = html.getAttribute('data-theme') === 'light';
  if (isLight) {
    html.removeAttribute('data-theme');
    localStorage.setItem('theme', 'dark');
    document.querySelector('meta[name="theme-color"]')?.setAttribute('content', '#000000');
  } else {
    html.setAttribute('data-theme', 'light');
    localStorage.setItem('theme', 'light');
    document.querySelector('meta[name="theme-color"]')?.setAttribute('content', '#f5f5f7');
  }
}

const clamp = (value, min = 0, max = 1) => Math.min(Math.max(value, min), max);
let animationFrame = null;

function sectionProgress(element) {
  const rect = element.getBoundingClientRect();
  return clamp((window.innerHeight - rect.top) / (window.innerHeight + rect.height));
}

function resetMotion() {
  document.querySelectorAll('.android-device').forEach((device) => {
    device.style.transform = '';
  });
}

function updateDeviceMotion() {
  animationFrame = null;
  if (reducedMotionQuery.matches || !desktopQuery.matches) {
    resetMotion();
    return;
  }

  const heroWrap = document.querySelector('.hero-sticky-wrap');
  const heroDevice = document.querySelector('.hero-device');
  if (heroWrap && heroDevice) {
    const rect = heroWrap.getBoundingClientRect();
    const scrollableDistance = Math.max(heroWrap.offsetHeight - window.innerHeight, 1);
    const progress = clamp(-rect.top / scrollableDistance);
    const arc = Math.sin(progress * Math.PI);
    heroDevice.style.transform = [
      'perspective(1500px)',
      `translateY(${(1 - arc) * 8}px)`,
      `rotateZ(${arc * 2.4}deg)`,
      `rotateY(${-arc * 5}deg)`,
      `rotateX(${arc * 0.6}deg)`,
      `scale(${0.98 + arc * 0.02})`
    ].join(' ');
  }

  document.querySelectorAll('.story-section .android-device[data-position]').forEach((device) => {
    const progress = sectionProgress(device);
    // A sine arc returns every phone to level on exit and reverses naturally on upward scroll.
    const arc = Math.sin(progress * Math.PI);
    const sign = device.dataset.position === 'left' ? -1 : 1;
    device.style.transform = [
      'perspective(1500px)',
      `translateY(${(1 - arc) * 10}px)`,
      `rotateZ(${sign * arc * 2.4}deg)`,
      `rotateY(${-sign * arc * 5.6}deg)`,
      `rotateX(${sign * arc * 0.6}deg)`,
      `scale(${0.97 + arc * 0.03})`
    ].join(' ');
  });
}

function requestMotionUpdate() {
  if (animationFrame === null) animationFrame = requestAnimationFrame(updateDeviceMotion);
}

function initHeroEntrance() {
  const hero = document.getElementById('hero-sticky');
  requestAnimationFrame(() => hero?.classList.add('entered'));
}

function initScrollReveal() {
  const elements = document.querySelectorAll('.reveal, .pop-card');
  if (reducedMotionQuery.matches) {
    elements.forEach((element) => element.classList.add('visible'));
    return;
  }
  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) entry.target.classList.add('visible');
    });
  }, { rootMargin: '-10% 0px', threshold: 0.1 });
  elements.forEach((element) => observer.observe(element));
}

function initGalleryNavigation() {
  const scroller = document.querySelector('.gallery-scroller');
  const slides = [...document.querySelectorAll('.gallery-slide')];
  const dots = [...document.querySelectorAll('.gallery-dot')];
  if (!scroller || !slides.length || dots.length !== slides.length) return;

  const maxScrollLeft = () => Math.max(scroller.scrollWidth - scroller.clientWidth, 0);

  const updateDots = () => {
    // The gallery shows a different number of cards at each breakpoint. Map the
    // real, scrollable track rather than guessing at a slide width and gap.
    const maximum = maxScrollLeft();
    const progress = maximum === 0 ? 0 : scroller.scrollLeft / maximum;
    const activeIndex = clamp(Math.round(progress * (slides.length - 1)), 0, slides.length - 1);
    dots.forEach((dot, index) => {
      dot.classList.toggle('active', index === activeIndex);
      dot.setAttribute('aria-selected', String(index === activeIndex));
      dot.tabIndex = index === activeIndex ? 0 : -1;
    });
  };

  scroller.addEventListener('scroll', updateDots, { passive: true });
  dots.forEach((dot, index) => dot.addEventListener('click', () => {
    const target = maxScrollLeft() * (index / (slides.length - 1));
    scroller.scrollTo({ left: target, behavior: 'smooth' });
  }));
  window.addEventListener('resize', updateDots, { passive: true });
  updateDots();
}

document.addEventListener('DOMContentLoaded', () => {
  initHeroEntrance();
  initScrollReveal();
  initGalleryNavigation();
  requestMotionUpdate();
});
window.addEventListener('scroll', requestMotionUpdate, { passive: true });
window.addEventListener('resize', requestMotionUpdate, { passive: true });
reducedMotionQuery.addEventListener('change', requestMotionUpdate);
desktopQuery.addEventListener('change', requestMotionUpdate);

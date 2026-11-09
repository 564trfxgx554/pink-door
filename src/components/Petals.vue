<template>
  <!-- Falling petals, ported 1:1 from the original login page:
       16 petals, pink shades, falling top to bottom, rotating. -->
  <div class="petals" aria-hidden="true">
    <span
      v-for="petal in petals"
      :key="petal.id"
      class="petal"
      :style="{
        width: petal.size + 'px',
        height: petal.size * 0.82 + 'px',
        left: petal.left + 'vw',
        background: petal.color,
        animationDuration: petal.duration + 's',
        animationDelay: petal.delay + 's',
        opacity: petal.opacity
      }"
    />
  </div>
</template>

<script setup>
// Same as the original page: 16 petals, colours cycling through
// #f78fb0, #f9a8c0, #f4538a, #ffc2d6. Values are fixed (not random on
// every render) so the page looks the same on every visit.
const colors = ['#f78fb0', '#f9a8c0', '#f4538a', '#ffc2d6']
const petals = Array.from({ length: 16 }, (_, i) => ({
  id: i,
  size: 7 + ((i * 37) % 10),
  left: (i * 61 + 5) % 100,
  color: colors[i % colors.length],
  duration: 9 + ((i * 7) % 11),
  delay: -((i * 53) % 15),
  opacity: 0.3 + ((i * 13) % 5) / 10
}))
</script>

<style scoped>
.petals {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}
.petal {
  position: absolute;
  top: -4vh;
  background: var(--pink);
  border-radius: 80% 4px 80% 4px;
  opacity: 0.55;
  animation: fall linear infinite;
}
@media (prefers-reduced-motion: reduce) {
  .petal { animation: none; display: none; }
}
</style>

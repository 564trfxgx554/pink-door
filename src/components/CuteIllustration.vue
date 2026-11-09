<script setup>
// The original illustration from the Pink Door login page (white cat
// with a big pink bow + pink rabbit in a pink hood, surrounded by
// roses, cherry blossoms and hearts) is inlined as a base64 data URI
// in ../assets/hero.js so the repo stays text-only.
import hero from '../assets/hero.js'
</script>

<template>
  <img
    :src="hero"
    class="hero-img"
    alt="粉色插画：戴着大蝴蝶结的白色小猫和穿粉色兜帽的小兔子，周围是玫瑰、樱花和爱心"
    width="1600"
    height="1600"
  />
</template>

<style scoped>
.hero-img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 26px;
  border: 3px solid var(--card);
  box-shadow: var(--shadow);
}
</style>

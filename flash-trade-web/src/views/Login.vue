<template>
  <main class="login-page">
    <div class="ambient ambient-one" aria-hidden="true"></div>
    <div class="ambient ambient-two" aria-hidden="true"></div>
    <div class="grid-overlay" aria-hidden="true"></div>

    <div class="login-layout">
      <section class="intro-panel" aria-labelledby="product-name">
        <header class="brand">
          <img class="brand-mark" :src="adminIcon" alt="" />
          <span class="brand-name">QuickTrade</span>
        </header>

        <div class="intro-copy">
          <h1 id="product-name">交易管理平台</h1>
          <p class="intro-description">
            账户、订单与风险，集中管理。
          </p>
        </div>

        <div class="visual-panel">
          <img class="market-visual" :src="visualUrl" alt="" aria-hidden="true" />
        </div>

      </section>

      <section class="form-panel" aria-labelledby="login-title">
        <div class="login-card">
          <div class="card-heading">
            <h2 id="login-title">管理员登录</h2>
          </div>

          <form class="login-form" @submit.prevent="login" novalidate>
            <div class="field-group">
              <label for="username">用户名</label>
              <div class="input-shell" :class="{ 'input-shell-error': loginError && !userId.trim() }">
                <svg class="field-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
                  <circle cx="10" cy="6.25" r="3.1" stroke="currentColor" stroke-width="1.5" />
                  <path d="M3.9 16.4c.55-2.65 2.95-4.4 6.1-4.4s5.55 1.75 6.1 4.4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
                </svg>
                <input
                  id="username"
                  v-model.trim="userId"
                  name="username"
                  type="text"
                  autocomplete="username"
                  required
                  @input="clearError"
                />
              </div>
            </div>

            <div class="field-group">
              <div class="label-row">
                <label for="password">密码</label>
              </div>
              <div class="input-shell">
                <svg class="field-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
                  <rect x="4.25" y="8.5" width="11.5" height="8" rx="1.8" stroke="currentColor" stroke-width="1.5" />
                  <path d="M6.75 8.5V6.25a3.25 3.25 0 0 1 6.5 0V8.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
                  <circle cx="10" cy="12.25" r=".9" fill="currentColor" />
                </svg>
                <input
                  id="password"
                  v-model="password"
                  name="password"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="current-password"
                  required
                  @input="clearError"
                />
                <button
                  class="password-toggle"
                  type="button"
                  :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  :aria-pressed="showPassword"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="!showPassword" viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="M2.5 10s2.7-4.25 7.5-4.25 7.5 4.25 7.5 4.25-2.7 4.25-7.5 4.25S2.5 10 2.5 10Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
                    <circle cx="10" cy="10" r="2" stroke="currentColor" stroke-width="1.5" />
                  </svg>
                  <svg v-else viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="m3 3 14 14M8.6 5.9c.45-.1.92-.15 1.4-.15 4.8 0 7.5 4.25 7.5 4.25a12 12 0 0 1-2.45 2.75M5.2 6.3C3.45 7.45 2.5 10 2.5 10s2.7 4.25 7.5 4.25c.7 0 1.34-.1 1.92-.28" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                </button>
              </div>
            </div>

            <p v-if="loginError" class="form-error" role="alert" aria-live="polite">
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                <circle cx="10" cy="10" r="7.25" stroke="currentColor" stroke-width="1.5" />
                <path d="M10 6.5v4.25M10 13.5h.01" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
              </svg>
              {{ loginError }}
            </p>

            <button class="login-button" type="submit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="button-spinner" aria-hidden="true"></span>
              <span>{{ isSubmitting ? "正在登录…" : "登录" }}</span>
              <svg v-if="!isSubmitting" viewBox="0 0 20 20" fill="none" aria-hidden="true">
                <path d="M4.25 10h11.5m-4.5-4.5 4.5 4.5-4.5 4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </button>
          </form>

          <div class="access-note">
            <svg viewBox="0 0 18 18" fill="none" aria-hidden="true">
              <path d="M9 2.25 14.5 4.5v3.9c0 3.35-2.3 5.8-5.5 7.35-3.2-1.55-5.5-4-5.5-7.35V4.5L9 2.25Z" stroke="currentColor" stroke-width="1.35" stroke-linejoin="round" />
              <path d="m6.8 8.9 1.45 1.45 3.05-3.1" stroke="currentColor" stroke-width="1.35" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span>仅限授权管理员访问</span>
          </div>
        </div>

      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { loginWebSession } from "@/utils/session";

const userId = ref("");
const password = ref("");
const showPassword = ref(false);
const isSubmitting = ref(false);
const loginError = ref("");
const adminIcon = `${import.meta.env.BASE_URL}quicktrade-admin.svg`;
const visualUrl = `${import.meta.env.BASE_URL}quicktrade-market-visual.svg`;
const route = useRoute();
const router = useRouter();

onMounted(() => {
  document.title = "QuickTrade · 管理端登录";
  document.body.classList.add("login-shell");
});

onBeforeUnmount(() => {
  document.body.classList.remove("login-shell");
});

const clearError = () => {
  loginError.value = "";
};

const getErrorStatus = (error: unknown): number | undefined => {
  if (typeof error !== "object" || error === null) return undefined;

  const record = error as Record<string, unknown>;
  if (typeof record.status === "number") return record.status;
  if (typeof record.statusCode === "number") return record.statusCode;
  const response = record.response;
  if (typeof response === "object" && response !== null && "status" in response) {
    const status = (response as Record<string, unknown>).status;
    return typeof status === "number" ? status : undefined;
  }
  return undefined;
};

const getErrorCode = (error: unknown): string | undefined => {
  if (typeof error !== "object" || error === null) return undefined;

  const record = error as Record<string, unknown>;
  if (record.code === "ECONNABORTED") return record.code;
  const response = record.response;
  if (typeof response === "object" && response !== null && "data" in response) {
    const body = (response as Record<string, unknown>).data;
    if (typeof body === "object" && body !== null && "code" in body) {
      const code = (body as Record<string, unknown>).code;
      if (typeof code === "string") return code;
    }
  }
  return typeof record.code === "string" ? record.code : undefined;
};

const getPostLoginRoute = () => {
  const redirect = route.query.redirect;
  if (typeof redirect !== "string"
      || !redirect.startsWith("/")
      || redirect.startsWith("//")
      || redirect.includes("\\")) {
    return "/dashboard";
  }

  const resolved = router.resolve(redirect);
  return resolved.matched.some(record => record.meta.requiresAuth)
    ? resolved.fullPath
    : "/dashboard";
};

const login = async () => {
  if (isSubmitting.value) return;

  if (!userId.value || !password.value) {
    loginError.value = "请填写用户名和密码。";
    return;
  }

  isSubmitting.value = true;
  loginError.value = "";

  try {
    await loginWebSession(userId.value, password.value);
    await router.replace(getPostLoginRoute());
  } catch (error: unknown) {
    const status = getErrorStatus(error);
    const code = getErrorCode(error);

    if (code === "ECONNABORTED") {
      loginError.value = "请求超时，请重试。";
    } else if (status === 429) {
      loginError.value = "尝试次数过多，请稍后重试。";
    } else {
      loginError.value = "登录失败，请检查账号密码后重试。";
    }
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
:global(body.login-shell) {
  display: block;
  min-width: 320px;
  min-height: 100vh;
  margin: 0;
  background: #07121d;
}

:global(body.login-shell #app) {
  width: 100%;
  max-width: none;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  text-align: left;
}

.login-page {
  --mint: #65dec2;
  position: relative;
  display: flex;
  box-sizing: border-box;
  min-height: 100vh;
  overflow: hidden;
  isolation: isolate;
  align-items: center;
  justify-content: center;
  padding: 32px clamp(28px, 7.3vw, 112px);
  color: #f3f7fa;
  background:
    radial-gradient(ellipse at 16% 12%, rgba(35, 91, 104, 0.19), transparent 38%),
    radial-gradient(ellipse at 78% 88%, rgba(25, 82, 77, 0.16), transparent 38%),
    linear-gradient(118deg, #091824 0%, #0b1722 47%, #0c1924 100%);
  font-family: Inter, "Segoe UI", "Microsoft YaHei", sans-serif;
}

.grid-overlay {
  position: absolute;
  z-index: -1;
  inset: 0;
  pointer-events: none;
  opacity: 0.2;
  background-image:
    linear-gradient(rgba(185, 218, 223, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(185, 218, 223, 0.035) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: linear-gradient(90deg, black, transparent 88%);
}

.ambient {
  position: absolute;
  z-index: -1;
  width: 420px;
  height: 420px;
  border: 1px solid rgba(98, 222, 194, 0.07);
  border-radius: 50%;
  pointer-events: none;
}

.ambient::before,
.ambient::after {
  position: absolute;
  content: "";
  border: 1px solid rgba(98, 222, 194, 0.055);
  border-radius: 50%;
  inset: 28px;
}

.ambient::after {
  inset: 62px;
}

.ambient-one {
  top: -260px;
  left: 36%;
}

.ambient-two {
  right: -310px;
  bottom: -250px;
  width: 520px;
  height: 520px;
}

.login-layout {
  display: grid;
  width: min(1190px, 100%);
  min-height: min(620px, calc(100vh - 64px));
  grid-template-columns: minmax(0, 1fr) minmax(380px, 470px);
  align-items: stretch;
  gap: clamp(72px, 10vw, 154px);
}

.intro-panel {
  display: flex;
  min-width: 0;
  flex-direction: column;
  padding: 2px 0 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  box-shadow: 0 0 26px rgba(101, 222, 194, 0.08);
}

.brand-name {
  color: #f4f8fa;
  font-size: 17px;
  font-weight: 650;
  letter-spacing: 0.025em;
}

.intro-copy {
  margin-top: 64px;
}

.intro-copy h1 {
  margin: 0;
  color: #f2f6f8;
  font-size: clamp(40px, 4.6vw, 56px);
  font-weight: 580;
  letter-spacing: -0.055em;
  line-height: 1.28;
}

.intro-description {
  max-width: none;
  margin: 22px 0 0;
  color: #a4b2be;
  font-size: 14px;
  letter-spacing: 0.015em;
  line-height: 1.9;
  white-space: nowrap;
}

.visual-panel {
  position: relative;
  margin-top: 32px;
  overflow: hidden;
  border: 1px solid rgba(182, 218, 222, 0.11);
  border-radius: 14px;
  background: #0b1b26;
  box-shadow: 0 22px 70px rgba(0, 0, 0, 0.12);
}

.market-visual {
  display: block;
  width: 100%;
  height: auto;
}

.form-panel {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
  padding: 6px 0 0;
}

.login-card {
  padding: 42px 38px 32px;
  border: 1px solid rgba(196, 220, 226, 0.15);
  border-radius: 18px;
  background: linear-gradient(145deg, rgba(19, 35, 47, 0.93), rgba(14, 28, 39, 0.9));
  box-shadow:
    0 32px 90px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(230, 248, 250, 0.035);
  backdrop-filter: blur(18px);
}

.card-heading h2 {
  margin: 0;
  color: #f1f5f7;
  font-size: 27px;
  font-weight: 590;
  letter-spacing: -0.035em;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 21px;
  margin-top: 34px;
}

.field-group label {
  display: block;
  margin-bottom: 9px;
  color: #d3dde2;
  font-size: 12px;
  font-weight: 520;
}

.input-shell {
  display: flex;
  height: 49px;
  align-items: center;
  gap: 11px;
  padding: 0 13px;
  border: 1px solid rgba(173, 199, 210, 0.16);
  border-radius: 9px;
  background: rgba(5, 17, 27, 0.48);
  transition: border-color 160ms ease, box-shadow 160ms ease, background 160ms ease;
}

.input-shell:focus-within {
  border-color: rgba(101, 222, 194, 0.67);
  background: rgba(5, 17, 27, 0.72);
  box-shadow: 0 0 0 3px rgba(101, 222, 194, 0.1);
}

.input-shell-error {
  border-color: rgba(241, 130, 130, 0.65);
}

.field-icon {
  width: 18px;
  height: 18px;
  flex: 0 0 auto;
  color: #81939e;
}

.input-shell input {
  width: 100%;
  height: 100%;
  min-width: 0;
  padding: 0;
  border: 0;
  outline: 0;
  color: #edf4f6;
  background: transparent;
  font: inherit;
  font-size: 13px;
}

.input-shell input::placeholder {
  color: #637582;
}

.password-toggle {
  display: grid;
  width: 30px;
  height: 32px;
  flex: 0 0 auto;
  padding: 0;
  border: 0;
  border-radius: 6px;
  color: #82949f;
  background: transparent;
  cursor: pointer;
  place-items: center;
  transition: color 160ms ease, background 160ms ease;
}

.password-toggle:hover {
  color: #dce8e9;
  background: rgba(220, 238, 239, 0.07);
}

.password-toggle svg {
  width: 18px;
  height: 18px;
}

.form-error {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: -7px 0 -3px;
  color: #f0a2a2;
  font-size: 11px;
  line-height: 1.6;
}

.form-error svg {
  width: 16px;
  height: 16px;
  flex: 0 0 auto;
  margin-top: 1px;
}

.login-button {
  display: flex;
  height: 49px;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 2px;
  border: 1px solid rgba(152, 245, 220, 0.38);
  border-radius: 9px;
  color: #06211f;
  background: linear-gradient(105deg, #67ddc2, #76e3c9);
  box-shadow: 0 8px 26px rgba(72, 196, 170, 0.16);
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.04em;
  transition: transform 160ms ease, box-shadow 160ms ease, filter 160ms ease;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.04);
  box-shadow: 0 11px 30px rgba(72, 196, 170, 0.23);
}

.login-button:focus-visible,
.password-toggle:focus-visible {
  outline: 2px solid #a0f4dc;
  outline-offset: 3px;
}

.login-button:disabled {
  cursor: wait;
  opacity: 0.78;
}

.login-button svg {
  width: 18px;
  height: 18px;
}

.button-spinner {
  width: 15px;
  height: 15px;
  border: 2px solid rgba(6, 33, 31, 0.28);
  border-top-color: #06211f;
  border-radius: 50%;
  animation: spin 750ms linear infinite;
}

.access-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 27px;
  color: #8d9da8;
  font-size: 10px;
  letter-spacing: 0.02em;
}

.access-note svg {
  width: 16px;
  height: 16px;
  color: #7bcbb8;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 940px) {
  .login-page {
    padding: 28px clamp(28px, 7vw, 72px);
  }

  .login-layout {
    min-height: min(620px, calc(100vh - 56px));
    grid-template-columns: minmax(0, 1fr) minmax(340px, 410px);
    gap: clamp(36px, 6vw, 70px);
  }

  .intro-copy {
    margin-top: 58px;
  }

  .intro-copy h1 {
    font-size: clamp(38px, 5.3vw, 50px);
  }

  .login-card {
    padding: 36px 29px 29px;
  }
}

@media (max-width: 720px) {
  .login-page {
    align-items: flex-start;
    padding: 25px 22px 22px;
  }

  .login-layout {
    width: min(440px, 100%);
    min-height: calc(100vh - 47px);
    grid-template-columns: minmax(0, 1fr);
    gap: 32px;
  }

  .intro-panel {
    min-height: auto;
  }

  .intro-copy {
    margin-top: 48px;
  }

  .intro-copy h1 {
    font-size: clamp(35px, 9vw, 46px);
  }

  .intro-description {
    max-width: 360px;
    margin-top: 14px;
    font-size: 12px;
    white-space: normal;
  }

  .visual-panel {
    display: none;
  }

  .form-panel {
    gap: 18px;
  }

  .login-card {
    padding: 30px 25px 25px;
  }

  .login-form {
    margin-top: 27px;
  }
}

@media (max-width: 380px) {
  .login-page {
    padding-right: 16px;
    padding-left: 16px;
  }

  .login-card {
    padding-right: 20px;
    padding-left: 20px;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    scroll-behavior: auto !important;
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>

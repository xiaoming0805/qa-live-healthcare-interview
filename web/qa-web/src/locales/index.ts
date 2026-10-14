import { createI18n } from 'vue-i18n';
import zhCN from './zh-CN.json';
import enUS from './en-US.json';

/** 支持的语言列表，供语言切换下拉菜单使用 */
export const SUPPORTED_LOCALES = [
  { value: 'zh-CN', label: '中文' },
  { value: 'en-US', label: 'English' },
];

export type LocaleCode = 'zh-CN' | 'en-US';

const STORAGE_KEY = 'qa-locale';
const DEFAULT_LOCALE: LocaleCode = 'zh-CN';

function isSupported(value: unknown): value is LocaleCode {
  return value === 'zh-CN' || value === 'en-US';
}

/** 优先取用户上次选择，其次按浏览器语言推断，最后回退到中文 */
function resolveInitialLocale(): LocaleCode {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (isSupported(saved)) return saved;
  } catch {
    // localStorage 不可用（如隐私模式）时忽略
  }
  return navigator.language?.toLowerCase().startsWith('zh') ? 'zh-CN' : DEFAULT_LOCALE;
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: resolveInitialLocale(),
  fallbackLocale: DEFAULT_LOCALE,
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
});

/** 切换语言：同步 i18n 实例、localStorage 与 <html lang> */
export function setLocale(locale: LocaleCode): void {
  i18n.global.locale.value = locale;
  document.documentElement.lang = locale;
  try {
    localStorage.setItem(STORAGE_KEY, locale);
  } catch {
    // 忽略持久化失败，语言仍在当前会话内生效
  }
}

export function getLocale(): LocaleCode {
  const current = i18n.global.locale.value;
  return isSupported(current) ? current : DEFAULT_LOCALE;
}

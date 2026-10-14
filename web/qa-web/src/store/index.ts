import { reactive } from 'vue';
import questionData from '../data/question-list.json';
import { fetchDoctors, loginDoctorApi } from '../api/doctor';
import {
  loginPatientApi,
  registerPatient as registerPatientApi,
  type PatientRegisterPayload,
} from '../api/patient';
import type { Doctor, Patient, Question } from '../types';

export type { Doctor, Patient, Question };

interface State {
  doctors: Doctor[];
  doctorsLoaded: boolean;
  doctorsLoading: boolean;
  doctorsError: string | null;
  questions: Question[];
  currentDoctor: Doctor | null;
  currentPatient: Patient | null;
}

const state = reactive<State>({
  // 医生数据已迁移到 qa-service-user + MySQL，此处不再从 JSON 初始化
  doctors: [],
  doctorsLoaded: false,
  doctorsLoading: false,
  doctorsError: null,
  // 问题数据仍保留在前端 JSON，不在迁移范围内
  questions: questionData as Question[],
  currentDoctor: null,
  currentPatient: null,
});

export const store = {
  state,

  /**
   * 从后端加载医生列表。重复调用会直接复用已有结果，除非 force = true。
   * 失败时把错误写入 state.doctorsError，不向上抛出，避免调用方遗漏 catch。
   */
  async loadDoctors(force = false): Promise<void> {
    if (state.doctorsLoading) return;
    if (state.doctorsLoaded && !force) return;

    state.doctorsLoading = true;
    state.doctorsError = null;
    try {
      state.doctors = await fetchDoctors();
      state.doctorsLoaded = true;
    } catch (error) {
      state.doctorsError = error instanceof Error ? error.message : '医生数据加载失败';
    } finally {
      state.doctorsLoading = false;
    }
  },

  async loginDoctor(username: string, password: string): Promise<Doctor | null> {
    try {
      const doctor = await loginDoctorApi(username, password);
      state.currentDoctor = doctor;
      return doctor;
    } catch {
      // 用户名或密码错误（401）统一按登录失败处理
      return null;
    }
  },

  logoutDoctor() {
    state.currentDoctor = null;
  },

  /** 患者登录：凭据由服务端校验，失败时抛出 ApiError 供页面展示 message */
  async loginPatient(username: string, password: string): Promise<Patient> {
    const patient = await loginPatientApi(username, password);
    state.currentPatient = patient;
    return patient;
  },

  /** 患者注册：注册成功后后端直接返回用户信息，视作已登录 */
  async registerPatient(payload: PatientRegisterPayload): Promise<Patient> {
    const patient = await registerPatientApi(payload);
    state.currentPatient = patient;
    return patient;
  },

  logoutPatient() {
    state.currentPatient = null;
  },

  getQuestionsByDoctor(doctorId: string): Question[] {
    return state.questions.filter(q => q.doctorId === doctorId);
  },

  getQuestionsByPatient(patientId: string): Question[] {
    return state.questions.filter(q => q.patientId === patientId);
  },

  addQuestion(question: Omit<Question, 'id' | 'submitTime' | 'status' | 'answer' | 'answerTime'>): Question {
    const newQuestion: Question = {
      ...question,
      id: `q${Date.now()}`,
      submitTime: new Date().toISOString(),
      status: 'pending',
      answer: null,
      answerTime: null,
    };
    state.questions.push(newQuestion);
    return newQuestion;
  },

  answerQuestion(questionId: string, answer: string) {
    const question = state.questions.find(q => q.id === questionId);
    if (question) {
      question.status = 'answered';
      question.answer = answer;
      question.answerTime = new Date().toISOString();
    }
  },

  markQuestionAsAnswered(questionId: string) {
    const question = state.questions.find(q => q.id === questionId);
    if (question) {
      question.status = 'answered';
      question.answer = '已口述解答';
      question.answerTime = new Date().toISOString();
    }
  },

  /** 依赖 loadDoctors() 已完成，供 Consultation.vue 在路由参数中取医生 */
  getDoctorByUsername(username: string): Doctor | undefined {
    return state.doctors.find(d => d.username === username);
  },

  getActiveDoctors(): Doctor[] {
    return state.doctors.filter(d => d.isActive);
  },

  getStatistics() {
    const totalDoctors = state.doctors.length;
    const totalQuestions = state.questions.length;
    const activeSessions = state.questions.filter(q => q.status === 'pending').length;
    const totalSessions = state.doctors.filter(d => d.isActive).length;

    return {
      totalDoctors,
      totalQuestions,
      activeSessions,
      totalSessions,
    };
  },
};

/**
 * 前后端共享的领域模型。
 * 单独抽成文件是为了避免 store 与 api 层互相 import 造成循环依赖。
 */

/** 医生。注意：不含 password，后端接口不会下发该字段 */
export interface Doctor {
  id: string;
  username: string;
  name: string;
  title: string;
  department: string;
  avatar: string;
  experience: string;
  specialties: string[];
  isActive: boolean;
}

/** 问诊用户（患者）。注意：不含 password，后端接口不会下发该字段 */
export interface Patient {
  id: string;
  username: string;
  name: string;
  birthday: string | null;
  phone: string | null;
  gender: string | null;
}

export interface Question {
  id: string;
  patientId: string;
  patientName: string;
  doctorId: string;
  doctorName: string;
  question: string;
  submitTime: string;
  status: 'pending' | 'answered';
  answer: string | null;
  answerTime: string | null;
}

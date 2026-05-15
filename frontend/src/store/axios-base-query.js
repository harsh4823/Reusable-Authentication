import { axiosInstance } from './axios-instance'

export const axiosBaseQuery = () => async ({ url, method = 'GET', data, params, headers }) => {
  try {
    const result = await axiosInstance({ url, method, data, params, headers })
    return { data: result.data }
  } catch (axiosError) {
    return {
      error: {
        status: axiosError.response?.status,
        data: axiosError.response?.data,
        message: axiosError.message,
      },
    }
  }
}
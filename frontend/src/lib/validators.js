import { z } from 'zod'

export const emailSchema = z.string().trim().email('Enter a valid email').max(255)

export const passwordSchema = z
  .string()
  .min(8, 'At least 8 characters')
  .regex(/[A-Z]/, 'Needs an uppercase letter')
  .regex(/[0-9]/, 'Needs a number')

export const fullNameSchema = z
  .string()
  .trim()
  .min(2, 'Too short')
  .max(80, 'Too long')
  .regex(/^[^\d]+$/, 'No numbers in your name')

export const realmNameSchema = z
  .string()
  .trim()
  .min(3)
  .max(50)
  .regex(/^[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/, 'Lowercase, numbers, hyphens; no leading/trailing hyphen')

export const displayNameSchema = z.string().trim().min(2).max(100)

export const redirectUriSchema = z.string().url('Must be a valid URL')

export const loginSchema = z.object({
  email: emailSchema,
  password: z.string().min(1, 'Password required'),
  remember: z.boolean().optional(),
})

export const registerSchema = z
  .object({
    fullName: fullNameSchema,
    email: emailSchema,
    password: passwordSchema,
    confirmPassword: z.string(),
  })
  .refine((d) => d.password === d.confirmPassword, {
    path: ['confirmPassword'],
    message: "Passwords don't match",
  })

export const onboardSchema = z
  .object({
    fullName: fullNameSchema,
    email: emailSchema,
    password: passwordSchema,
    confirmPassword: z.string(),
    appName: displayNameSchema,
    realmName: realmNameSchema,
    redirectUri: redirectUriSchema,
  })
  .refine((d) => d.password === d.confirmPassword, {
    path: ['confirmPassword'],
    message: "Passwords don't match",
  })

// z.infer type exports removed — not needed in JSX

export function passwordStrength(pw) {
  let score = 0
  if (pw.length >= 8) score++
  if (/[A-Z]/.test(pw)) score++
  if (/[0-9]/.test(pw)) score++
  if (/[^A-Za-z0-9]/.test(pw)) score++
  if (pw.length >= 12) score++
  if (score <= 2) return 'weak'
  if (score <= 3) return 'fair'
  return 'strong'
}
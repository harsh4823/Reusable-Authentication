import { jwtDecode } from 'jwt-decode'

export function decodeToken(token) {
  try {
    return jwtDecode(token)
  } catch {
    return null
  }
}

export function rolesFromToken(token) {
  if (!token) return []
  const claims = decodeToken(token)
  if (!claims) return []
  if (Array.isArray(claims.roles)) return claims.roles
  // Spring Security style — authorities claim
  const auth = claims.authorities
  if (Array.isArray(auth)) return auth.filter((a) => typeof a === 'string')
  return []
}
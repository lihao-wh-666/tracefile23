import JSEncrypt from 'jsencrypt'

export function encryptPassword(publicKey, password) {
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  return encryptor.encrypt(password)
}

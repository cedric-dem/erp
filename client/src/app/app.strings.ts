export const APP_STRINGS = {
  subtitle: 'Homepage.',
  loginTab: 'Sign In',
  registerTab: 'Create Account',
  usernameLabel: 'User',
  passwordLabel: 'Password',
  loginSubmit: 'Log In',
  registerSubmit: 'Create Account',
  errors: {
    loginEmpty: 'Please enter a username and password.',
    loginInvalid: 'Invalid credentials.',
    registerEmpty: 'Please choose a username and password.',
    registerExists: 'This username already exists.'
  },
  success: {
    registerCreated: 'Account created successfully. You can now sign in.'
  }
} as const;

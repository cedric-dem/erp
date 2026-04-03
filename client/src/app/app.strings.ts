export const APP_STRINGS = {
  subtitle: 'Homepage.',
  loginTab: 'Sign In',
  registerTab: 'Create Account',
  usernameLabel: 'User',
  passwordLabel: 'Password',
  projectLabel: 'Project',
  projectActionLabel: 'Project Access',
  joinProject: 'Join Existing',
  createProject: 'Create New',
  loginSubmit: 'Log In',
  registerSubmit: 'Create Account',
  errors: {
    loginEmpty: 'Please enter a username and password.',
    loginInvalid: 'Invalid credentials.',
    registerEmpty: 'Please choose a username, password, and project.',
    registerExists: 'This username already exists.',
    registerProjectNotFound: 'Project not found. Please choose an existing project or create a new one.'
  },
  success: {
    registerCreated: 'Account created successfully. You can now sign in.'
  }
} as const;

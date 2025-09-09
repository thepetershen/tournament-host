import React from 'react';

const LoginSuccessPage = () => (
  <div style={{
    maxWidth: '400px',
    margin: '80px auto',
    padding: '2rem 2.5rem',
    background: '#fff',
    borderRadius: '12px',
    boxShadow: '0 2px 16px rgba(0,0,0,0.08)',
    textAlign: 'center',
  }}>
    <h2>Registration & Login Successful!</h2>
    <p>Welcome! You have successfully registered and logged in.</p>
    <a href="/" style={{ color: '#2d72d9', textDecoration: 'underline' }}>Go to Home</a>
  </div>
);

export default LoginSuccessPage;

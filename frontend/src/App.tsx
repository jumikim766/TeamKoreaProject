import { useEffect, useState } from 'react';
import './App.css';

function App() {
  const [message, setMessage] = useState('불러오는 중...');
  const [error, setError] = useState('');

  useEffect(() => {
    fetch('http://localhost:8080/api/hello')
      .then((response) => {
        if (!response.ok) {
          throw new Error('서버 응답 오류');
        }
        return response.text();
      })
      .then((data) => {
        setMessage(data);
      })
      .catch((err) => {
        console.error(err);
        setError('백엔드 연결 실패');
      });
  }, []);

  return (
    <div style={{ padding: '40px', fontSize: '20px' }}>
      <h1>React + Spring Boot 연결 테스트</h1>
      {error ? <p>{error}</p> : <p>{message}</p>}
    </div>
  );
}

export default App;
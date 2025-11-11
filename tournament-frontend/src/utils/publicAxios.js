import axios from 'axios';

const publicAxios = axios.create({
  baseURL: 'http://localhost:8080',
});

export default publicAxios;

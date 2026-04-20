import axios from 'axios';

const client = axios.create({ baseURL: 'http://localhost:8080/spring-boot-api' });

export default client;

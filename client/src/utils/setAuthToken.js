import axios from "axios";
import jwtDecode from "jwt-decode";

export default (token = null) => {
	if (token) {
		localStorage.roles = JSON.stringify(jwtDecode(token).authorities);
		axios.defaults.headers.common.authorization = `Bearer ${token}`;
	} else {
		delete axios.defaults.headers.common.authorization;
	}
};

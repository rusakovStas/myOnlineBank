import { GET_USERS, ADD_USER, DELETE_USER } from "../actions/types";

export default function user(state = [], action) {
	switch (action.type) {
		case GET_USERS:
			return action.users;
		case ADD_USER:
			return state.concat(action.user);
		case DELETE_USER:
			return state.filter(item => item.user_id !== action.user.user_id);
		default:
			return state;
	}
}

import { USER_LOGGED_IN, USER_LOGGED_OUT, USER_ROLES } from "../actions/types";

export default function user(state = {}, action = {}) {
	switch (action.type) {
		case USER_LOGGED_IN:
			return action.user;
		case USER_ROLES:
			return { ...state, roles: action.roles };
		case USER_LOGGED_OUT:
			return {};
		default:
			return state;
	}
}

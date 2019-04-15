import {
	GET_ACCOUNTS,
	ADD_ACCOUNT,
	DELETE_ACCOUNT,
	UPDATE_ACCOUNT
} from "../actions/types";

export default function user(state = [], action) {
	switch (action.type) {
		case GET_ACCOUNTS:
			return action.accounts;
		case ADD_ACCOUNT:
			return state.concat(action.account);
		case UPDATE_ACCOUNT:
			return state.map(item =>
				item.id === action.account.id
					? {
							...item,
							amount: action.account.amount,
							name: action.account.name
					  }
					: item
			);
		case DELETE_ACCOUNT:
			return state.filter(item => item.id !== action.id);
		default:
			return state;
	}
}

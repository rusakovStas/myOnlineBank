import { combineReducers } from "redux";
import user from "./user";
import users from "./users";
import accounts from "./accounts";

export default combineReducers({
	user,
	users,
	accounts
});

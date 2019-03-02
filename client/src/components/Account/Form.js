import React from "react";
import { Container, ListGroup, ListGroupItem } from "reactstrap";
import Account from "./Account";

class AccountForm extends React.Component {
	state = {
		accounts: [
			{ number: "4876 **** **** 1231", name: "Grandma", index: 0 },
			{ number: "4876 **** **** 1231", name: "Common", index: 1 },
			{ number: "4876 **** **** 1231", name: "Love account", index: 2 }
		],
		suggestions: [
			{
				name: "Alabama",
				population: 4780127,
				capital: "Montgomery",
				region: "South"
			},
			{
				name: "Alaska",
				population: 710249,
				capital: "Juneau",
				region: "West"
			},
			{
				name: "Arizona",
				population: 6392307,
				capital: "Phoenix",
				region: "West"
			},
			{
				name: "Arkansas",
				population: 2915958,
				capital: "Little Rock",
				region: "South"
			},
			{
				name: "California",
				population: 37254503,
				capital: "Sacramento",
				region: "West"
			}
		],
		collapse: -1
	};

	toggle = index => {
		this.setState({
			collapse: this.state.collapse === Number(index) ? -1 : Number(index)
		});
	};

	render() {
		return (
			<div>
				<Container>
					<h1>Accounts</h1>
					<ListGroup>
						{this.state.accounts.map(acc => (
							<ListGroupItem>
								<Account
									account={acc}
									open={this.state.collapse === acc.index}
									toggle={this.toggle}
									suggestions={this.state.suggestions}
								/>
							</ListGroupItem>
						))}
					</ListGroup>
				</Container>
			</div>
		);
	}
}

export default AccountForm;

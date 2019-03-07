import React from "react";
import { Container, Row, Col, Jumbotron } from "reactstrap";
import Account from "./Account";

class AccountForm extends React.Component {
	state = {
		accounts: [
			{
				number: "4876 1323 2343 1231",
				owner: "user",
				amount: "1000",
				index: 0
			},
			{
				number: "4876 1323 2343 1231",
				owner: "user",
				name: "Common",
				amount: "1000",
				index: 1
			},
			{
				number: "4876 1323 2343 1231",
				owner: "user",
				name: "Love account",
				amount: "1000",
				index: 2
			}
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
					<Jumbotron className="d-flex justify-content-center p-0">
						<h1 className="display-4">Accounts</h1>
					</Jumbotron>
					<Row>
						<Col
							sm="12"
							md={{ size: 10, offset: 1 }}
							className="mb-2 mt-2"
						/>
						{this.state.accounts.map(acc => (
							<Col
								sm="12"
								md={{ size: 10, offset: 1 }}
								className="mb-1"
							>
								<Account
									account={acc}
									open={this.state.collapse === acc.index}
									toggle={this.toggle}
									suggestions={this.state.suggestions}
								/>
							</Col>
						))}
					</Row>
				</Container>
			</div>
		);
	}
}

export default AccountForm;

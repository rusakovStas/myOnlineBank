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
				userName: "Alabama",
				account: 4780127,
				index: 1
			},
			{
				userName: "Alaska",
				account: 710249,
				index: 2
			},
			{
				userName: "Arizona",
				account: "63***92307",
				index: 3
			},
			{
				userName: "Arkansas",
				account: 2915958,
				index: 4
			},
			{
				userName: "California",
				account: 37254503,
				index: 5
			},
			{
				userName: "California",
				account: 37254504,
				index: 6
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

import React from "react";
import ScrollAnimation from "react-animate-on-scroll";

const HomePage = () => (
	<div>
		<section className="intro-section">
			<div className="row h-100">
				<div className="col-sm-12 my-auto">
					<h1>Welcome to innovation online bank!</h1>
				</div>
				<div className="col-sm-12 mt-auto">
					<h6>Scrool below to see instruction</h6>
				</div>
			</div>
		</section>

		<section className="account-section">
			<div className="row h-100">
				<div className="col-sm-12 my-auto">
					<ScrollAnimation animateIn="fadeIn">
						<h1>Instruction for Accounts tab</h1>
					</ScrollAnimation>
				</div>
			</div>
		</section>
		<section>
			<div className="row h-100">
				<div className="col-sm-12 my-auto">
					<ScrollAnimation animateIn="fadeIn">
						<h1>Instruction for Admin tab</h1>
					</ScrollAnimation>
				</div>
			</div>
		</section>
	</div>
);

export default HomePage;

import ClayLayout from "@clayui/layout";
import ClayButton from "@clayui/button";
import React from "react";
import { Link } from "react-router-dom";
import { ClassNameButton } from "../App";

export function MachingLearning() {
  return (
    <React.Fragment>
      <ClayLayout.ContainerFluid view>
        <ClayLayout.Row>
          <Card
            title="Train your model"
            description="When working on a Machine learning project flexibility and reusability are very important to make your life easier while developing the solution. Find the best way to structure your project files can be difficult when you are a beginner or when the project becomes big. Sometime you may end up duplicate or rewrite some part of your project which is not professional as a Data Scientist or Machine learning Engineer."
            link="train-your-model"
            nameButton="Train your model"
          />
          <Card
            title="Configure your model from hugging face"
            description="Hugging Face is a community and data science platform that provides: Tools that enable users to build, train and deploy ML models based on open source (OS) code and technologies."
            link="hugging-face-view"
            nameButton="Configure your model"
          />
        </ClayLayout.Row>
      </ClayLayout.ContainerFluid>
    </React.Fragment>
  );
}

function Card({ title, description, link, nameButton }: { title: string; description: string; link: string; nameButton: string }) {
  return (
    <div className="col-md-12">
      <div className="card">
        <div className="card-body">
          <div className="card-title" style={{ margin: "16px" }}>
            <h1>{title}</h1>
          </div>
          <div className="card-text" style={{ margin: "16px" }}>
            {description}
          </div>
          <Link to={link}>
            <ClayButton className={ClassNameButton} displayType="primary" style={{ margin: "16px" }}>
              {nameButton}
            </ClayButton>
          </Link>
        </div>
      </div>
    </div>
  );
}

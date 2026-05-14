type StepCardProps = {
  step: string;
  icon: string;
  title: string;
  description: string;
};

const StepCard = ({
  step,
  icon,
  title,
  description,
}: StepCardProps) => {
  return (
    <div className="step-card">

      <div className="step-number">
        STEP {step}
      </div>

      <div className="step-icon">
        {icon}
      </div>

      <h3>{title}</h3>

      <p>{description}</p>

    </div>
  );
};

export default StepCard;
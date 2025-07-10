
export function EmptySpace({
  title, description, extraClass = "",
}: {
  title: string;
  description: string;
  extraClass?: string;
}) {
  const classNames = `empty-state ${extraClass}`;
  return (
    <div
      className={classNames}
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "30vh",
        flexDirection: "column",
      }}
    >
      <div>
        <h3 className="empty-state-title">{title}</h3>
      </div>
      <div className="empty-state-description">{description}</div>
    </div>
  );
}

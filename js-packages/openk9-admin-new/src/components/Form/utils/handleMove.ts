function not(a: { value: string; label: string }[], b: string[]) {
  return a.filter(({ value }) => !b.includes(value));
}

function intersection(a: { value: string; label: string }[], b: string[]) {
  return a.filter(({ value }) => b.includes(value));
}

export const handleMove = (
  direction: 'right' | 'left',
  association: {
    items: { label: string; value: string }[][];
    setItems: React.Dispatch<React.SetStateAction<{ label: string; value: string }[][]>>;
  },
  leftSelected: string[],
  rightSelected: string[],
  setLeftSelected: React.Dispatch<React.SetStateAction<string[]>>,
  setRightSelected: React.Dispatch<React.SetStateAction<string[]>>,
) => {
  const newLeft = direction === 'right' 
    ? not(association.items[0], leftSelected) 
    : [...association.items[0], ...intersection(association.items[1], rightSelected)];

  const newRight = direction === 'right' 
    ? [...association.items[1], ...intersection(association.items[0], leftSelected)] 
    : not(association.items[1], rightSelected);

  association.setItems([newLeft, newRight]);
  direction === 'right' ? setLeftSelected([]) : setRightSelected([]);
}; 
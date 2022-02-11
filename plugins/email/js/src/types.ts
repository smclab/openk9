export type EmailResultItem = {
  email: {
    cc?: string;
    date: number; // timestamp
    htmlBody: string;
    body: string;
    from: string;
    subject: string;
    to: string;
  };
};

/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import express from "express";

const app = express();
app.use(express.json());

app.post("/exec", async (req, res) => {
  try {
    const code = `(${req.body.code})(${JSON.stringify(req.body)})`;
    const result = eval(code);
    res.send(result);
  } catch (err) {
    console.error(err);
    res.status(500);
    res.send(JSON.stringify(err));
  }
});

app.listen(3000, "0.0.0.0", () => {
  console.log("Listening on port 3000");
});

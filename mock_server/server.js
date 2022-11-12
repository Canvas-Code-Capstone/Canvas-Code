const express = require("express")
const app = express()
const cors = require('cors')
const axios = require('axios')

app.use(cors({
    origin: '*'
}))

app.get("/hello", cors(), function (req,res) {
    res.json({data:"hello"})
})

app.listen(3002, () => {
    console.log("listening port 3002")
})
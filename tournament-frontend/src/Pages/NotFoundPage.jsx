import {Link } from "react-router-dom"

function NotFoundPage () {
    return (
        <div>
            <div>
                Not found page. 
            </div>
            <Link to="/"> Home</Link>

        </div>
        
    )
}

export default NotFoundPage;
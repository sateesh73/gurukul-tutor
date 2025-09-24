import { Link } from "react-router";

const NAV_LINKS = [
  { name: "Home", path: "/" },
  { name: "AI Tutor", path: "/" },
  { name: "Progress", path: "/" },
  { name: "About", path: "/" }
];

const Navbar = () => {
  return (
    <nav className="navbar">
      <div className="flex space-x-10">
        {NAV_LINKS.map((link) => (
          <Link
            key={link.name}
            to={link.path}
            className="text-2xl font-bold text-gradient"
          >
            {link.name}
          </Link>
        ))}
      </div>
      <Link to="#" className="primary-button w-fit">
        Log In
      </Link>
    </nav>
  )
}
export default Navbar

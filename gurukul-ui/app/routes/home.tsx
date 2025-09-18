import type { Route } from "./+types/home";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Gurukul" },
    { name: "description", content: "smart gurukul for tutoring" },
  ];
}

export default function Home() {
  return <main>
  <section>
  <div>
  <h1>Gurukul</h1>
  </div>
  </section>
  </main>;
}
